package com.example.project_management_app.controller;

import com.example.project_management_app.pipeline.scheduler.MetricsScheduler;
import com.example.project_management_app.pipeline.service.RealTimeMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private RealTimeMetricsService realTimeMetricsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MetricsScheduler metricsScheduler;

    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics() {
        return ResponseEntity.ok(realTimeMetricsService.getCurrentMetrics());
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<List<Map<String, Object>>> getDailyMetrics(@PathVariable String date) {
        LocalDate targetDate = LocalDate.parse(date);

        String sql = """
            SELECT * FROM task_metrics
            WHERE date = ?
            ORDER BY team_name, project_name
            """;

        List<Map<String, Object>> metrics = jdbcTemplate.queryForList(sql, targetDate);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/team-performance")
    public ResponseEntity<List<Map<String, Object>>> getTeamPerformance() {
        String sql = """
            SELECT
                team_name,
                SUM(completed_tasks) as total_completed,
                SUM(total_tasks) as total_tasks,
                ROUND(AVG(completion_rate), 2) as avg_completion_rate,
                SUM(overdue_tasks) as total_overdue,
                COUNT(DISTINCT project_id) as active_projects
            FROM task_metrics
            WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            GROUP BY team_name
            ORDER BY avg_completion_rate DESC
            """;

        List<Map<String, Object>> performance = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/user-velocity/{userId}")
    public ResponseEntity<Map<String, Object>> getUserVelocity(@PathVariable Long userId) {
        String sql = """
            SELECT
                COUNT(*) as tasks_completed,
                MIN(created_at) as first_task,
                MAX(updated_at) as last_task
            FROM tasks
            WHERE assigned_user_id = ?
            AND status = 'DONE'
            AND updated_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
            """;

        Map<String, Object> velocity = jdbcTemplate.queryForMap(sql, userId);
        return ResponseEntity.ok(velocity);
    }

    @PostMapping("/trigger-daily-metrics")
    public ResponseEntity<String> triggerDailyMetrics() {
        try {
            metricsScheduler.runDailyMetricsJob();
            return ResponseEntity.ok("Daily metrics job triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}