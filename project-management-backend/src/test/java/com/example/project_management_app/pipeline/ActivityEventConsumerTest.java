package com.example.project_management_app.pipeline;

import com.example.project_management_app.pipeline.kafka.ActivityEventConsumer;
import com.example.project_management_app.pipeline.model.UserActivityEvent;
import com.example.project_management_app.pipeline.service.RealTimeMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ActivityEventConsumerTest {

    @Mock
    private RealTimeMetricsService realTimeMetricsService;

    @InjectMocks
    private ActivityEventConsumer activityEventConsumer;

    private UserActivityEvent taskCreatedEvent;
    private UserActivityEvent taskCompletedEvent;
    private UserActivityEvent userLoginEvent;
    private UserActivityEvent unknownEvent;

    @BeforeEach
    void setUp() {
        activityEventConsumer.initHandlers();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("priority", "HIGH");
        metadata.put("dueDate", "2025-12-31");

        taskCreatedEvent = UserActivityEvent.builder()
                .userId(1L)
                .username("Mahmoud")
                .action("TASK_CREATED")
                .entityType("TASK")
                .entityId(100L)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();

        Map<String, Object> completedMetadata = new HashMap<>();
        completedMetadata.put("completedInHours", 24.5);
        completedMetadata.put("originalPriority", "HIGH");

        taskCompletedEvent = UserActivityEvent.builder()
                .userId(1L)
                .username("Mahmoud")
                .action("TASK_COMPLETED")
                .entityType("TASK")
                .entityId(100L)
                .metadata(completedMetadata)
                .timestamp(Instant.now())
                .build();

        userLoginEvent = UserActivityEvent.builder()
                .userId(1L)
                .username("Mahmoud")
                .action("USER_LOGIN")
                .entityType("USER")
                .entityId(1L)
                .metadata(null)
                .timestamp(Instant.now())
                .build();

        unknownEvent = UserActivityEvent.builder()
                .userId(1L)
                .username("Mahmoud")
                .action("UNKNOWN_ACTION")
                .entityType("UNKNOWN")
                .entityId(999L)
                .metadata(null)
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void testConsumeTaskCreatedEvent() {
        activityEventConsumer.consumeActivityEvent(taskCreatedEvent);

        verify(realTimeMetricsService, times(1)).incrementTasksCreatedToday();
        verify(realTimeMetricsService, never()).incrementTasksCompletedToday();
        verify(realTimeMetricsService, never()).updateUserVelocity(anyLong());
        verify(realTimeMetricsService, never()).recordUserActive(anyLong());
    }

    @Test
    void testConsumeTaskCompletedEvent() {
        activityEventConsumer.consumeActivityEvent(taskCompletedEvent);

        verify(realTimeMetricsService, times(1)).incrementTasksCompletedToday();
        verify(realTimeMetricsService, times(1)).updateUserVelocity(1L);
        verify(realTimeMetricsService, never()).incrementTasksCreatedToday();
        verify(realTimeMetricsService, never()).recordUserActive(anyLong());
    }

    @Test
    void testConsumeUserLoginEvent() {
        activityEventConsumer.consumeActivityEvent(userLoginEvent);

        verify(realTimeMetricsService, times(1)).recordUserActive(1L);
        verify(realTimeMetricsService, never()).incrementTasksCreatedToday();
        verify(realTimeMetricsService, never()).incrementTasksCompletedToday();
        verify(realTimeMetricsService, never()).updateUserVelocity(anyLong());
    }

    @Test
    void testConsumeUnknownActionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            activityEventConsumer.consumeActivityEvent(unknownEvent);
        });

        verify(realTimeMetricsService, never()).incrementTasksCreatedToday();
        verify(realTimeMetricsService, never()).incrementTasksCompletedToday();
        verify(realTimeMetricsService, never()).updateUserVelocity(anyLong());
        verify(realTimeMetricsService, never()).recordUserActive(anyLong());
    }

    @Test
    void testConsumeMultipleEvents() {
        activityEventConsumer.consumeActivityEvent(taskCreatedEvent);
        activityEventConsumer.consumeActivityEvent(taskCompletedEvent);
        activityEventConsumer.consumeActivityEvent(userLoginEvent);

        verify(realTimeMetricsService, times(1)).incrementTasksCreatedToday();
        verify(realTimeMetricsService, times(1)).incrementTasksCompletedToday();
        verify(realTimeMetricsService, times(1)).updateUserVelocity(1L);
        verify(realTimeMetricsService, times(1)).recordUserActive(1L);
    }

    @Test
    void testConsumeTaskCreatedWithDifferentUser() {
        UserActivityEvent differentUserEvent = UserActivityEvent.builder()
                .userId(2L)
                .username("Katya")
                .action("TASK_CREATED")
                .entityType("TASK")
                .entityId(200L)
                .metadata(null)
                .timestamp(Instant.now())
                .build();

        activityEventConsumer.consumeActivityEvent(differentUserEvent);

        verify(realTimeMetricsService, times(1)).incrementTasksCreatedToday();
        verify(realTimeMetricsService, never()).updateUserVelocity(anyLong());
    }
}
