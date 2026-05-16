package com.example.project_management_app.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMetric {
    private Long metricId;
    private LocalDate date;
    private Long projectId;
    private String projectName;
    private Long teamId;
    private String teamName;

    // Task counts
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer overdueTasks;

    // Completion rate
    private Double completionRate;

    // Time metrics
    private Double avgCompletionTimeHours;
    private Double avgTaskAgeDays;

    // User metrics
    private Long activeUsers;
    private Long uniqueAssignees;

    private LocalDateTime calculatedAt;
}