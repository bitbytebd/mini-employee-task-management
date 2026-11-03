package com.fifo.employeetask.service;

import com.fifo.employeetask.dto.UserSummaryDto;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserSummaryDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserSummaryDto toDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                roles
        );
    }
}
