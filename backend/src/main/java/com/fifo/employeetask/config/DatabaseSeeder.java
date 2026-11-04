package com.fifo.employeetask.config;

import com.fifo.employeetask.entity.Role;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDemoUsers() {
        return args -> {
            seedUser("admin@fifo.local", "admin", "System Administrator", "admin123", Role.ADMIN, null);
            User manager = seedUser("manager@fifo.local", "manager", "Project Manager", "manager123", Role.MANAGER, null);
            seedUser("employee1@fifo.local", "emp1", "Employee One", "employee123", Role.EMPLOYEE, manager);
            seedUser("employee2@fifo.local", "emp2", "Employee Two", "employee123", Role.EMPLOYEE, manager);
        };
    }

    private User seedUser(String email, String username, String fullName, String rawPassword, Role role, User manager) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setEnabled(true);
            user.setRoles(Set.of(role));
            user.setManager(manager);
            return userRepository.save(user);
        });
    }
}
