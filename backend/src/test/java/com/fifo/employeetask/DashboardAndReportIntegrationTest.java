package com.fifo.employeetask;

import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.TaskPriority;
import com.fifo.employeetask.entity.TaskStatus;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import com.fifo.employeetask.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardAndReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void dashboard_scopes_stats_by_role() throws Exception {
        User manager = user("manager@fifo.local");
        createTask(manager, user("employee1@fifo.local"), TaskStatus.PENDING);
        createTask(manager, user("employee2@fifo.local"), TaskStatus.DONE);

        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", tokenFor("manager@fifo.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCount").value(2))
                .andExpect(jsonPath("$.taskCount").value(2))
                .andExpect(jsonPath("$.completedCount").value(1));

        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", tokenFor("employee1@fifo.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskCount").value(1))
                .andExpect(jsonPath("$.pendingCount").value(1));
    }

    @Test
    void admin_and_manager_can_download_scoped_pdf_but_employee_is_denied() throws Exception {
        User manager = user("manager@fifo.local");
        createTask(manager, user("employee1@fifo.local"), TaskStatus.PENDING);

        mockMvc.perform(get("/api/reports/tasks/pdf").header("Authorization", tokenFor("admin@fifo.local")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/reports/tasks/pdf").header("Authorization", tokenFor("manager@fifo.local")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/reports/tasks/pdf").header("Authorization", tokenFor("employee1@fifo.local")))
                .andExpect(status().isForbidden());
    }

    private Task createTask(User createdBy, User assignee, TaskStatus status) {
        Task task = new Task();
        task.setTitle("Dashboard task " + System.nanoTime());
        task.setDescription("Dashboard integration test");
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(status);
        task.setCreatedBy(createdBy);
        task.setAssignee(assignee);
        task.setDeadline(LocalDate.now().plusDays(7));
        return taskRepository.save(task);
    }

    private User user(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    private String tokenFor(String email) {
        User user = user(email);
        return "Bearer " + jwtService.generateToken(user.getEmail(), user.getRoles());
    }
}
