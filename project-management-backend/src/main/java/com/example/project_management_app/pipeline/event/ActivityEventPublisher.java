package com.example.project_management_app.pipeline.event;

import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.pipeline.model.UserActivityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ActivityEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishTaskCreated(Task task, User assignedBy) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("priority", task.getPriority());
        metadata.put("dueDate", task.getDueDate());

        UserActivityEvent event = UserActivityEvent.builder()
                .userId(assignedBy.getId())
                .username(assignedBy.getUsername())
                .action("TASK_CREATED")
                .entityType("TASK")
                .entityId(task.getId())
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();

        applicationEventPublisher.publishEvent(event);
    }

    public void publishTaskCompleted(Task task, User completedBy) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("completedInHours", calculateCompletionTime(task));
        metadata.put("originalPriority", task.getPriority());

        UserActivityEvent event = UserActivityEvent.builder()
                .userId(completedBy.getId())
                .username(completedBy.getUsername())
                .action("TASK_COMPLETED")
                .entityType("TASK")
                .entityId(task.getId())
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();

        applicationEventPublisher.publishEvent(event);
    }

    private Double calculateCompletionTime(Task task) {
        if (task.getCreatedAt() != null && task.getUpdatedAt() != null) {
            long hours = java.time.Duration.between(task.getCreatedAt(), task.getUpdatedAt()).toHours();
            return (double) hours;
        }
        return null;
    }
}