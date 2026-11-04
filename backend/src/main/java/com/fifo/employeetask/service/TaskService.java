package com.fifo.employeetask.service;

import com.fifo.employeetask.dto.TaskCreateRequest;
import com.fifo.employeetask.dto.TaskDto;
import com.fifo.employeetask.dto.TaskStatusUpdateRequest;
import com.fifo.employeetask.dto.TaskUpdateRequest;
import com.fifo.employeetask.entity.Role;
import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.TaskPriority;
import com.fifo.employeetask.entity.TaskStatus;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Page<TaskDto> getTasksForCurrentUser(User currentUser, TaskStatus status, TaskPriority priority,
                                                 Long assigneeId, Pageable pageable) {
        return taskRepository.findAll(scopedSpecification(currentUser, status, priority, assigneeId), pageable)
                .map(this::toDto);
    }

    public TaskDto getTask(Long taskId, User currentUser) {
        Task task = findTask(taskId);
        if (!canViewTask(currentUser, task)) {
            throw new AccessDeniedException("You are not allowed to view this task");
        }
        return toDto(task);
    }

    public TaskDto createTask(TaskCreateRequest request, User currentUser) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(request.getPriority());
        task.setCreatedBy(currentUser);
        task.setDeadline(request.getDeadline());

        task.setAssignee(resolveAssignableEmployee(currentUser, request.getAssigneeId()));

        return toDto(taskRepository.save(task));
    }

    public TaskDto updateTask(Long taskId, TaskUpdateRequest request, User currentUser) {
        Task task = findTask(taskId);
        if (!hasRole(currentUser, Role.ADMIN) && (!hasRole(currentUser, Role.MANAGER)
                || !task.getCreatedBy().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You are not allowed to edit this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setAssignee(resolveAssignableEmployee(currentUser, request.getAssigneeId()));
        return toDto(taskRepository.save(task));
    }

    public TaskDto updateStatus(Long taskId, TaskStatusUpdateRequest request, User currentUser) {
        Task task = findTask(taskId);

        if (!hasRole(currentUser, Role.EMPLOYEE) || task.getAssignee() == null
                || !task.getAssignee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to modify this task");
        }

        if (!isNextStatus(task.getStatus(), request.getStatus())) {
            throw new IllegalStateException("Task status must advance from PENDING to IN_PROGRESS to DONE");
        }

        task.setStatus(request.getStatus());
        return toDto(taskRepository.save(task));
    }

    public void deleteTask(Long taskId) {
        taskRepository.delete(findTask(taskId));
    }

    private User resolveAssignableEmployee(User currentUser, Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
        if (!assignee.isEnabled() || !hasRole(assignee, Role.EMPLOYEE)) {
            throw new IllegalArgumentException("Tasks can be assigned only to active employees");
        }
        if (!hasRole(currentUser, Role.ADMIN) && (!hasRole(currentUser, Role.MANAGER)
                || assignee.getManager() == null || !assignee.getManager().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Managers can assign tasks only to employees on their team");
        }
        return assignee;
    }

    private boolean isNextStatus(TaskStatus current, TaskStatus requested) {
        return (current == TaskStatus.PENDING && requested == TaskStatus.IN_PROGRESS)
                || (current == TaskStatus.IN_PROGRESS && requested == TaskStatus.DONE);
    }

    private boolean hasRole(User user, Role role) {
        return user.getRoles().contains(role);
    }

    private boolean canViewTask(User currentUser, Task task) {
        return hasRole(currentUser, Role.ADMIN)
                || (hasRole(currentUser, Role.MANAGER) && task.getCreatedBy().getId().equals(currentUser.getId()))
                || (hasRole(currentUser, Role.EMPLOYEE) && task.getAssignee().getId().equals(currentUser.getId()));
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
    }

    private Specification<Task> scopedSpecification(User currentUser, TaskStatus status, TaskPriority priority, Long assigneeId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!hasRole(currentUser, Role.ADMIN)) {
                if (hasRole(currentUser, Role.MANAGER)) {
                    predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), currentUser.getId()));
                } else if (hasRole(currentUser, Role.EMPLOYEE)) {
                    predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), currentUser.getId()));
                }
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
