package com.fifo.employeetask.dto;

import com.fifo.employeetask.entity.TaskPriority;
import com.fifo.employeetask.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long assigneeId;
    private String assigneeName;
    private Long createdById;
    private String createdByName;
    private LocalDate deadline;
}
