package com.fifo.employeetask.controller;

import com.fifo.employeetask.dto.TaskCreateRequest;
import com.fifo.employeetask.dto.TaskDto;
import com.fifo.employeetask.dto.TaskStatusUpdateRequest;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TaskDto>> getTasks(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getTasksForCurrentUser(currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateRequest request,
                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, currentUser));
    }

    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<TaskDto> updateStatus(@PathVariable Long taskId,
                                               @Valid @RequestBody TaskStatusUpdateRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateStatus(taskId, request, currentUser));
    }
}
