package com.fifo.employeetask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifo.employeetask.dto.LoginRequest;
import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.TaskPriority;
import com.fifo.employeetask.entity.TaskStatus;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import com.fifo.employeetask.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void login_returns_200_for_valid_credentials() throws Exception {
        LoginRequest request = new LoginRequest("admin@fifo.local", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("admin@fifo.local"));
    }

    @Test
    void login_returns_401_for_invalid_credentials() throws Exception {
        LoginRequest request = new LoginRequest("admin@fifo.local", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missing_jwt_returns_401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_jwt_returns_401() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden_role_access_returns_403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearerToken("manager@fifo.local")))
                .andExpect(status().isForbidden());
    }

    @Test
    void expired_jwt_returns_401() throws Exception {
        String expiredToken = Jwts.builder()
                .setSubject("employee1@fifo.local")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor("test-secret-key-for-jwt-validation".getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void manager_cannot_use_employee_only_status_endpoint() throws Exception {
        mockMvc.perform(put("/api/tasks/999/status")
                        .header("Authorization", bearerToken("manager@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_cannot_use_employee_only_status_endpoint() throws Exception {
        mockMvc.perform(put("/api/tasks/999/status")
                        .header("Authorization", bearerToken("admin@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void employee_can_update_own_task() throws Exception {
        Task task = createTask("employee1@fifo.local");

        mockMvc.perform(put("/api/tasks/{taskId}/status", task.getId())
                        .header("Authorization", bearerToken("employee1@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void employee_cannot_update_another_employees_task() throws Exception {
        Task task = createTask("employee1@fifo.local");

        mockMvc.perform(put("/api/tasks/{taskId}/status", task.getId())
                        .header("Authorization", bearerToken("employee2@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalid_status_transition_returns_409() throws Exception {
        Task task = createTask("employee1@fifo.local");

        mockMvc.perform(put("/api/tasks/{taskId}/status", task.getId())
                        .header("Authorization", bearerToken("employee1@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isConflict());
    }

    private Task createTask(String assigneeEmail) {
        User manager = userRepository.findByEmail("manager@fifo.local").orElseThrow();
        User assignee = userRepository.findByEmail(assigneeEmail).orElseThrow();

        Task task = new Task();
        task.setTitle("Security test task");
        task.setDescription("Used only by the integration test");
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedBy(manager);
        task.setAssignee(assignee);
        task.setDeadline(LocalDate.now().plusDays(7));
        return taskRepository.save(task);
    }

    private String bearerToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return "Bearer " + jwtService.generateToken(user.getEmail(), user.getRoles());
    }
}
