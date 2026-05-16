package com.example.project_management_app.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityEvent {
    private Long userId;
    private String username;
    private String action;
    private String entityType;
    private Long entityId;
    private Map<String, Object> metadata;
    private Instant timestamp;
}