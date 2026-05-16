package com.example.project_management_app.pipeline.kafka;

import com.example.project_management_app.pipeline.model.UserActivityEvent;
import com.example.project_management_app.pipeline.service.RealTimeMetricsService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class ActivityEventConsumer {

    @Autowired
    private RealTimeMetricsService realTimeMetricsService;

    private final Map<String, Consumer<UserActivityEvent>> actionHandlers = new ConcurrentHashMap<>();

    @PostConstruct
    public void initHandlers() {
        actionHandlers.put("TASK_CREATED", event ->
                realTimeMetricsService.incrementTasksCreatedToday()
        );

        actionHandlers.put("TASK_COMPLETED", event -> {
            realTimeMetricsService.incrementTasksCompletedToday();
            realTimeMetricsService.updateUserVelocity(event.getUserId());
        });

        actionHandlers.put("USER_LOGIN", event ->
                realTimeMetricsService.recordUserActive(event.getUserId())
        );
    }

    @KafkaListener(topics = "user-activities", groupId = "project-metrics-group")
    public void consumeActivityEvent(UserActivityEvent event) {
        Consumer<UserActivityEvent> handler = actionHandlers.get(event.getAction());

        if (handler != null) {
            handler.accept(event);
        } else {
            throw new IllegalArgumentException("Unknown action: " + event.getAction());
        }
    }
}