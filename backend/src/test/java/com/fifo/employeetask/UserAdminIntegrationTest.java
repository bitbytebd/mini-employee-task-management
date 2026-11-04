package com.fifo.employeetask;

import com.fifo.employeetask.entity.User;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanTestUsers() {
        List.of("testemp1", "testemp2", "testemp3").forEach(username ->
                userRepository.findByUsername(username).ifPresent(userRepository::delete));
    }

    @Test
    void admin_can_create_find_update_and_delete_user() throws Exception {
        String email = "testemp1@fifo.local";
        String username = "testemp1";

        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Test Employee One", username, email, "employee-password", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        Number responseId = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        Long id = responseId.longValue();
        mockMvc.perform(get("/api/users/{id}", id).header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(put("/api/users/{id}", id)
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Updated Employee", username, email, "", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Employee"))
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(delete("/api/users/{id}", id).header("Authorization", adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicate_email_returns_conflict() throws Exception {
        String email = "testemp2@fifo.local";

        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("First User", "testemp2", email, "employee-password", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Second User", "testemp3", email, "employee-password", true)))
                .andExpect(status().isConflict());
    }

    @Test
    void admin_can_search_with_pagination_and_employee_is_forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", adminToken())
                        .param("page", "0")
                        .param("size", "2")
                        .param("search", "admin@fifo.local"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@fifo.local"));

        mockMvc.perform(get("/api/users").header("Authorization", tokenFor("employee1@fifo.local")))
                .andExpect(status().isForbidden());
    }

    private String userJson(String fullName, String username, String email, String password, boolean enabled) {
        String passwordField = password.isBlank() ? "" : ",\"password\":\"" + password + "\"";
        return "{\"fullName\":\"" + fullName + "\",\"username\":\"" + username
                + "\",\"email\":\"" + email + "\",\"roles\":[\"EMPLOYEE\"],\"enabled\":" + enabled + passwordField + "}";
    }

    private String adminToken() {
        return tokenFor("admin@fifo.local");
    }

    private String tokenFor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return "Bearer " + jwtService.generateToken(user.getEmail(), user.getRoles());
    }
}
