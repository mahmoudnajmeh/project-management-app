package com.example.project_management_app.dto;

import com.example.project_management_app.entity.Task;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.Priority priority;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long projectId;
    private String projectName;
    private Long assignedUserId;
    private String assignedUserName;
    private String assignedUserFirstName;
    private String assignedUserLastName;

    public TaskResponseDto() {}

    public TaskResponseDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.dueDate = task.getDueDate();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();

        if (task.getProject() != null) {
            this.projectId = task.getProject().getId();
            this.projectName = task.getProject().getName();
        }

        if (task.getAssignedUser() != null) {
            this.assignedUserId = task.getAssignedUser().getId();
            this.assignedUserName = task.getAssignedUser().getUsername();
            this.assignedUserFirstName = task.getAssignedUser().getFirstName();
            this.assignedUserLastName = task.getAssignedUser().getLastName();
        }
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
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }
    public String getAssignedUserName() { return assignedUserName; }
    public void setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; }
    public String getAssignedUserFirstName() { return assignedUserFirstName; }
    public void setAssignedUserFirstName(String assignedUserFirstName) { this.assignedUserFirstName = assignedUserFirstName; }
    public String getAssignedUserLastName() { return assignedUserLastName; }
    public void setAssignedUserLastName(String assignedUserLastName) { this.assignedUserLastName = assignedUserLastName; }
}