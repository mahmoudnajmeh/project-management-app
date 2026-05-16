package com.example.project_management_app.pipeline;

import com.example.project_management_app.pipeline.service.RealTimeMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RealTimeMetricsServiceTest {

    @InjectMocks
    private RealTimeMetricsService realTimeMetricsService;

    @BeforeEach
    void setUp() {
        realTimeMetricsService.resetDailyMetrics();
    }

    @Test
    void testIncrementTasksCreatedToday() {
        assertEquals(0, getTasksCreatedToday());

        realTimeMetricsService.incrementTasksCreatedToday();
        assertEquals(1, getTasksCreatedToday());

        realTimeMetricsService.incrementTasksCreatedToday();
        assertEquals(2, getTasksCreatedToday());
    }

    @Test
    void testIncrementTasksCompletedToday() {
        assertEquals(0, getTasksCompletedToday());

        realTimeMetricsService.incrementTasksCompletedToday();
        assertEquals(1, getTasksCompletedToday());

        realTimeMetricsService.incrementTasksCompletedToday();
        assertEquals(2, getTasksCompletedToday());
    }

    @Test
    void testUpdateUserVelocity() {
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(2L);

        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        double avgVelocity = (double) metrics.get("averageUserVelocity");
        assertEquals(1.5, avgVelocity);
    }

    @Test
    void testRecordUserActive() {
        realTimeMetricsService.recordUserActive(1L);
        realTimeMetricsService.recordUserActive(2L);
        realTimeMetricsService.recordUserActive(3L);

        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        assertEquals(3, metrics.get("activeUsersCount"));
    }

    @Test
    void testResetDailyMetrics() {
        realTimeMetricsService.incrementTasksCreatedToday();
        realTimeMetricsService.incrementTasksCompletedToday();
        realTimeMetricsService.recordUserActive(1L);
        realTimeMetricsService.updateUserVelocity(1L);

        assertTrue(getTasksCreatedToday() > 0);
        assertTrue(getTasksCompletedToday() > 0);

        realTimeMetricsService.resetDailyMetrics();

        assertEquals(0, getTasksCreatedToday());
        assertEquals(0, getTasksCompletedToday());
        assertEquals(0, getActiveUsersCount());
    }

    @Test
    void testGetCurrentMetrics() {
        realTimeMetricsService.incrementTasksCreatedToday();
        realTimeMetricsService.incrementTasksCompletedToday();
        realTimeMetricsService.recordUserActive(1L);
        realTimeMetricsService.recordUserActive(2L);
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(2L);

        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();

        assertNotNull(metrics.get("date"));
        assertEquals(1, metrics.get("tasksCreatedToday"));
        assertEquals(1, metrics.get("tasksCompletedToday"));
        assertEquals(2, metrics.get("activeUsersCount"));
        assertEquals(1.5, (double) metrics.get("averageUserVelocity"));
    }

    @Test
    void testUserVelocityWithNoActivities() {
        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        assertEquals(0.0, (double) metrics.get("averageUserVelocity"));
    }

    @Test
    void testMultipleUsersVelocity() {
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(1L);
        realTimeMetricsService.updateUserVelocity(2L);
        realTimeMetricsService.updateUserVelocity(2L);
        realTimeMetricsService.updateUserVelocity(3L);

        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        assertEquals(2.0, (double) metrics.get("averageUserVelocity"));
    }

    private int getTasksCreatedToday() {
        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        return (int) metrics.get("tasksCreatedToday");
    }

    private int getTasksCompletedToday() {
        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        return (int) metrics.get("tasksCompletedToday");
    }

    private int getActiveUsersCount() {
        Map<String, Object> metrics = realTimeMetricsService.getCurrentMetrics();
        return (int) metrics.get("activeUsersCount");
    }
}