package com.fifo.employeetask.controller;

import com.fifo.employeetask.dto.TaskCreateRequest;
import com.fifo.employeetask.dto.TaskDto;
import com.fifo.employeetask.dto.TaskStatusUpdateRequest;
import com.fifo.employeetask.dto.TaskUpdateRequest;
import com.fifo.employeetask.entity.TaskPriority;
import com.fifo.employeetask.entity.TaskStatus;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<Page<TaskDto>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getTasksForCurrentUser(currentUser, status, priority, assigneeId, pageable));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long taskId, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getTask(taskId, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateRequest request,
                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, currentUser));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long taskId,
                                              @Valid @RequestBody TaskUpdateRequest request,
                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, currentUser));
    }

    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TaskDto> updateStatus(@PathVariable Long taskId,
                                               @Valid @RequestBody TaskStatusUpdateRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateStatus(taskId, request, currentUser));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
