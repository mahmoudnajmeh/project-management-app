package com.example.project_management_app.dto;

import com.example.project_management_app.entity.Task;
import jakarta.validation.constraints.NotBlank;

public class TaskDto {
    private Long id;

    @NotBlank
    private String title;

    private String description;
    private Task.TaskStatus status;
    private Task.Priority priority;
    private String dueDate;
    private Long projectId;
    private Long assignedUserId;

    public TaskDto() {}

    public TaskDto(String title, String description, Long projectId, Long assignedUserId) {
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.assignedUserId = assignedUserId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Task.TaskStatus getStatus() { return status; }
    public void setStatus(Task.TaskStatus status) { this.status = status; }
    public Task.Priority getPriority() { return priority; }
    public void setPriority(Task.Priority priority) { this.priority = priority; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }
}