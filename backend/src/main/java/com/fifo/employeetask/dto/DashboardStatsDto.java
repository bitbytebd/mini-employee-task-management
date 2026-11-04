package com.fifo.employeetask.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardStatsDto {
    private long userCount;
    private long taskCount;
    private long pendingCount;
    private long inProgressCount;
    private long completedCount;
}
