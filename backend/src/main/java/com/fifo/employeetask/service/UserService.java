package com.fifo.employeetask.service;

import com.fifo.employeetask.dto.UserCreateRequest;
import com.fifo.employeetask.dto.UserSummaryDto;
import com.fifo.employeetask.dto.UserUpdateRequest;
import com.fifo.employeetask.entity.Role;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserSummaryDto> findUsers(String search, Pageable pageable) {
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable)
                .map(this::toDto);
    }

    public UserSummaryDto findUserById(Long id) {
        return toDto(findUser(id));
    }

    public java.util.List<UserSummaryDto> findDirectReports(User manager) {
        return userRepository.findByManager(manager).stream()
                .filter(User::isEnabled)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSummaryDto createUser(UserCreateRequest request) {
        validateUniqueFields(request.getEmail(), request.getUsername(), null);

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(request.getRoles());
        user.setEnabled(request.getEnabled());
        user.setManager(resolveManager(user, request.getManagerId()));
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserSummaryDto updateUser(Long id, UserUpdateRequest request) {
        User user = findUser(id);
        validateUniqueFields(request.getEmail(), request.getUsername(), id);

        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRoles(request.getRoles());
        user.setEnabled(request.getEnabled());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setManager(resolveManager(user, request.getManagerId()));
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUser(id);
        if (taskRepository.existsByAssigneeOrCreatedBy(user, user) || userRepository.existsByManager(user)) {
            throw new IllegalStateException("User cannot be deleted while referenced by tasks or team members");
        }
        userRepository.delete(user);
    }

    public UserSummaryDto toDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getStoredUsername(),
                user.getFullName(),
                roles,
                user.isEnabled(),
                user.getManager() != null ? user.getManager().getId() : null,
                user.getManager() != null ? user.getManager().getFullName() : null
        );
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void validateUniqueFields(String email, String username, Long userId) {
        boolean emailExists = userId == null ? userRepository.existsByEmail(email) : userRepository.existsByEmailAndIdNot(email, userId);
        boolean usernameExists = userRepository.findByUsername(username)
                .filter(existingUser -> userId == null || !existingUser.getId().equals(userId))
                .isPresent();
        if (emailExists || usernameExists) {
            throw new IllegalStateException("Email or username is already in use");
        }
    }

    private User resolveManager(User user, Long managerId) {
        if (managerId == null) {
            return null;
        }
        if (user.getId() != null && user.getId().equals(managerId)) {
            throw new IllegalArgumentException("A user cannot manage themselves");
        }
        if (!user.getRoles().contains(Role.EMPLOYEE)) {
            throw new IllegalArgumentException("Only employees can be assigned a manager");
        }

        User manager = findUser(managerId);
        if (!manager.getRoles().contains(Role.MANAGER)) {
            throw new IllegalArgumentException("Assigned manager must have the MANAGER role");
        }
        if (wouldCreateManagementCycle(user, manager)) {
            throw new IllegalArgumentException("Manager assignment would create a circular relationship");
        }
        return manager;
    }

    private boolean wouldCreateManagementCycle(User user, User manager) {
        User current = manager;
        while (current != null) {
            if (user.getId() != null && user.getId().equals(current.getId())) {
                return true;
            }
            current = current.getManager();
        }
        return false;
    }
}
