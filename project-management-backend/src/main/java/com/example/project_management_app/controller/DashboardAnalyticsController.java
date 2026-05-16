package com.example.project_management_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/analytics")
public class DashboardAnalyticsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/summary")
    public Map<String, Object> getDashboardSummary() {
        String tasksSql = """
            SELECT 
                COUNT(CASE WHEN status = 'DONE' THEN 1 END) as completed,
                COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress,
                COUNT(CASE WHEN status = 'TODO' THEN 1 END) as todo,
                COUNT(CASE WHEN due_date < NOW() AND status != 'DONE' THEN 1 END) as overdue
            FROM tasks
            """;

        String projectSql = """
            SELECT 
                COUNT(*) as total,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
                COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress
            FROM projects
            """;

        String userSql = """
            SELECT 
                COUNT(*) as total,
                COUNT(CASE WHEN last_activity > DATE_SUB(NOW(), INTERVAL 15 MINUTE) THEN 1 END) as online
            FROM users
            """;

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", jdbcTemplate.queryForMap(tasksSql));
        response.put("projects", jdbcTemplate.queryForMap(projectSql));
        response.put("users", jdbcTemplate.queryForMap(userSql));
        response.put("timestamp", LocalDateTime.now());

        return response;
    }
}