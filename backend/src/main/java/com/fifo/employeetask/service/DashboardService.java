package com.fifo.employeetask.service;

import com.fifo.employeetask.dto.DashboardStatsDto;
import com.fifo.employeetask.entity.Role;
import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.TaskStatus;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public DashboardStatsDto getStats(User currentUser) {
        List<Task> tasks;
        long userCount;

        if (currentUser.getRoles().contains(Role.ADMIN)) {
            tasks = taskRepository.findAll();
            userCount = userRepository.count();
        } else if (currentUser.getRoles().contains(Role.MANAGER)) {
            Set<Long> teamMemberIds = userRepository.findByManager(currentUser).stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            tasks = taskRepository.findByCreatedBy(currentUser).stream()
                    .filter(task -> task.getAssignee() != null && teamMemberIds.contains(task.getAssignee().getId()))
                    .toList();
            userCount = teamMemberIds.size();
        } else {
            tasks = taskRepository.findByAssignee(currentUser);
            userCount = 1;
        }

        return new DashboardStatsDto(
                userCount,
                tasks.size(),
                countByStatus(tasks, TaskStatus.PENDING),
                countByStatus(tasks, TaskStatus.IN_PROGRESS),
                countByStatus(tasks, TaskStatus.DONE)
        );
    }

    private long countByStatus(List<Task> tasks, TaskStatus status) {
        return tasks.stream().filter(task -> task.getStatus() == status).count();
    }
}
