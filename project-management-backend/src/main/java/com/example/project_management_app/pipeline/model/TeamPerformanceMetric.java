package com.example.project_management_app.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceMetric {
    private Long metricId;
    private LocalDate date;
    private Long teamId;
    private String teamName;

    // Velocity metrics
    private Integer tasksCompleted;
    private Integer tasksStarted;
    private Double velocityTrend;

    // Quality metrics
    private Integer reopensCount;
    private Double firstTimeRightRate;

    // Workload metrics
    private Integer avgTasksPerMember;
    private Integer overdueTasksCount;

    // Productivity metrics
    private Double productivityScore;
    private Map<Long, Integer> memberTaskCounts;

    private LocalDateTime calculatedAt;
}
