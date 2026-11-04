package com.fifo.employeetask;

import com.fifo.employeetask.entity.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void admin_can_create_and_delete_task() throws Exception {
        User employee = user("employee1@fifo.local");
        String response = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", tokenFor("admin@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson("Admin task", employee.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        Number responseId = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        mockMvc.perform(delete("/api/tasks/{id}", responseId.longValue())
                        .header("Authorization", tokenFor("admin@fifo.local")))
                .andExpect(status().isNoContent());
    }

    @Test
    void manager_can_assign_direct_report_but_not_outside_team() throws Exception {
        User employee = user("employee1@fifo.local");
        User outsideEmployee = createEmployee("outside" + System.nanoTime() + "@fifo.local", null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", tokenFor("manager@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson("Team task", employee.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", tokenFor("manager@fifo.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson("Outside task", outsideEmployee.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_cannot_read_another_managers_task() throws Exception {
        User employee = user("employee1@fifo.local");
        User otherManager = createManager("other-manager" + System.nanoTime() + "@fifo.local");
        Task task = createTask(otherManager, employee, TaskStatus.PENDING);

        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .header("Authorization", tokenFor("manager@fifo.local")))
                .andExpect(status().isForbidden());
    }

    @Test
    void employee_list_and_detail_access_are_scoped_to_own_tasks() throws Exception {
        User manager = user("manager@fifo.local");
        User employeeOne = user("employee1@fifo.local");
        User employeeTwo = user("employee2@fifo.local");
        createTask(manager, employeeOne, TaskStatus.PENDING);
        Task otherTask = createTask(manager, employeeTwo, TaskStatus.PENDING);

        mockMvc.perform(get("/api/tasks").header("Authorization", tokenFor("employee1@fifo.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/tasks/{id}", otherTask.getId())
                        .header("Authorization", tokenFor("employee1@fifo.local")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_filters_tasks_with_pagination() throws Exception {
        User manager = user("manager@fifo.local");
        User employee = user("employee1@fifo.local");
        createTask(manager, employee, TaskStatus.PENDING);
        createTask(manager, employee, TaskStatus.PENDING);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", tokenFor("admin@fifo.local"))
                        .param("status", "PENDING")
                        .param("assigneeId", employee.getId().toString())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(1));
    }

    private Task createTask(User createdBy, User assignee, TaskStatus status) {
        Task task = new Task();
        task.setTitle("Task " + System.nanoTime());
        task.setDescription("Task integration test");
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(status);
        task.setCreatedBy(createdBy);
        task.setAssignee(assignee);
        task.setDeadline(LocalDate.now().plusDays(7));
        return taskRepository.save(task);
    }

    private User createManager(String email) {
        User manager = new User();
        manager.setEmail(email);
        manager.setUsername(email.substring(0, email.indexOf('@')));
        manager.setFullName("Other Manager");
        manager.setPassword(passwordEncoder.encode("password123"));
        manager.setEnabled(true);
        manager.setRoles(Set.of(Role.MANAGER));
        return userRepository.save(manager);
    }

    private User createEmployee(String email, User manager) {
        User employee = new User();
        employee.setEmail(email);
        employee.setUsername(email.substring(0, email.indexOf('@')));
        employee.setFullName("Outside Employee");
        employee.setPassword(passwordEncoder.encode("password123"));
        employee.setEnabled(true);
        employee.setRoles(Set.of(Role.EMPLOYEE));
        employee.setManager(manager);
        return userRepository.save(employee);
    }

    private User user(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    private String taskJson(String title, Long assigneeId) {
        return "{\"title\":\"" + title + "\",\"description\":\"Task description\",\"priority\":\"MEDIUM\",\"assigneeId\":"
                + assigneeId + ",\"deadline\":\"" + LocalDate.now().plusDays(7) + "\"}";
    }

    private String tokenFor(String email) {
        User user = user(email);
        return "Bearer " + jwtService.generateToken(user.getEmail(), user.getRoles());
    }
}
