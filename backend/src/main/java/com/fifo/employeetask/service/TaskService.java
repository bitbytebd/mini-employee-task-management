package com.fifo.employeetask.service;

import com.fifo.employeetask.dto.TaskCreateRequest;
import com.fifo.employeetask.dto.TaskDto;
import com.fifo.employeetask.dto.TaskStatusUpdateRequest;
import com.fifo.employeetask.entity.Role;
import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.TaskStatus;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskDto> getTasksForCurrentUser(User currentUser) {
        if (hasRole(currentUser, Role.ADMIN)) {
            return taskRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
        }

        if (hasRole(currentUser, Role.MANAGER)) {
            return taskRepository.findByCreatedByOrAssignee(currentUser, currentUser).stream().map(this::toDto).collect(Collectors.toList());
        }

        return taskRepository.findByAssignee(currentUser).stream().map(this::toDto).collect(Collectors.toList());
    }

    public TaskDto createTask(TaskCreateRequest request, User currentUser) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(request.getPriority());
        task.setCreatedBy(currentUser);
        task.setDeadline(request.getDeadline());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
            validateAssignment(currentUser, assignee);
            task.setAssignee(assignee);
        }

        return toDto(taskRepository.save(task));
    }

    public TaskDto updateStatus(Long taskId, TaskStatusUpdateRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!hasRole(currentUser, Role.EMPLOYEE) || task.getAssignee() == null
                || !task.getAssignee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to modify this task");
        }

        if (!isNextStatus(task.getStatus(), request.getStatus())) {
            throw new IllegalArgumentException("Task status must advance from PENDING to IN_PROGRESS to DONE");
        }

        task.setStatus(request.getStatus());
        return toDto(taskRepository.save(task));
    }

    private void validateAssignment(User currentUser, User assignee) {
        if (!hasRole(currentUser, Role.ADMIN) && (!hasRole(currentUser, Role.MANAGER)
                || assignee.getManager() == null || !assignee.getManager().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Managers can assign tasks only to employees on their team");
        }
    }

    private boolean isNextStatus(TaskStatus current, TaskStatus requested) {
        return (current == TaskStatus.PENDING && requested == TaskStatus.IN_PROGRESS)
                || (current == TaskStatus.IN_PROGRESS && requested == TaskStatus.DONE);
    }

    private boolean hasRole(User user, Role role) {
        return user.getRoles().contains(role);
    }

    private TaskDto toDto(Task task) {
        User assignee = task.getAssignee();
        User createdBy = task.getCreatedBy();

        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                assignee != null ? assignee.getId() : null,
                assignee != null ? assignee.getFullName() : null,
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getFullName() : null,
                task.getDeadline()
        );
    }
}
