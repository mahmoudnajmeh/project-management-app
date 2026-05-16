package com.example.project_management_app.pipeline.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RealTimeMetricsService {

    private final AtomicInteger tasksCreatedToday = new AtomicInteger(0);
    private final AtomicInteger tasksCompletedToday = new AtomicInteger(0);
    private final Map<Long, Integer> userVelocities = new ConcurrentHashMap<>();
    private final Map<Long, Long> userLastActive = new ConcurrentHashMap<>();

    public void incrementTasksCreatedToday() {
        tasksCreatedToday.incrementAndGet();
    }

    public void incrementTasksCompletedToday() {
        tasksCompletedToday.incrementAndGet();
    }

    public void updateUserVelocity(Long userId) {
        userVelocities.merge(userId, 1, Integer::sum);
    }

    public void recordUserActive(Long userId) {
        userLastActive.put(userId, System.currentTimeMillis());
    }

    public Map<String, Object> getCurrentMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("tasksCreatedToday", tasksCreatedToday.get());
        metrics.put("tasksCompletedToday", tasksCompletedToday.get());
        metrics.put("activeUsersCount", userLastActive.size());
        metrics.put("averageUserVelocity", userVelocities.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));
        metrics.put("date", LocalDate.now());

        return metrics;
    }

    public void resetDailyMetrics() {
        tasksCreatedToday.set(0);
        tasksCompletedToday.set(0);
        userLastActive.clear();
    }
}