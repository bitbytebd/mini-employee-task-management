package com.fifo.employeetask.controller;

import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.service.TaskReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class TaskReportController {

    private final TaskReportService taskReportService;

    @GetMapping(value = "/tasks/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> downloadTaskReport(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("tasks-report.pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(taskReportService.createTaskReport(currentUser));
    }
}
