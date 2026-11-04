package com.fifo.employeetask.controller;

import com.fifo.employeetask.dto.DashboardStatsDto;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dashboardService.getStats(currentUser));
    }
}
