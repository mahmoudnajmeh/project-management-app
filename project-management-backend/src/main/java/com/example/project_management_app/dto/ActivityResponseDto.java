package com.example.project_management_app.dto;

import com.example.project_management_app.entity.Notification;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ActivityResponseDto {
    private Long id;
    private String type;
    private String action;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private UserSummaryDto user;
    private EntitySummaryDto entity;

    public ActivityResponseDto() {}

    public ActivityResponseDto(Notification notification, User sender) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.content = notification.getContent();
        this.createdAt = notification.getCreatedAt();

        if (sender != null) {
            this.user = new UserSummaryDto(sender);
        }

        if (notification.getEntityId() != null && notification.getEntityType() != null) {
            this.entity = new EntitySummaryDto(
                    notification.getEntityId(),
                    notification.getEntityType()
            );
        }

        // Set action based on type
        switch (notification.getType()) {
            case "TASK_CREATED":
                this.action = "created task";
                break;
            case "TASK_UPDATED":
                this.action = "updated task";
                break;
            case "TASK_COMPLETED":
                this.action = "completed task";
                break;
            case "TASK_REASSIGNED":
                this.action = "reassigned task";
                break;
            case "TASK_DELETED":
                this.action = "deleted task";
                break;
            case "PROJECT_CREATED":
                this.action = "created project";
                break;
            case "PROJECT_UPDATED":
                this.action = "updated project";
                break;
            case "PROJECT_DELETED":
                this.action = "deleted project";
                break;
            case "USER_JOINED":
                this.action = "joined the team";
                break;
            default:
                this.action = "performed action";
        }
    }

    public ActivityResponseDto(Project project, String action, User user) {
        this.id = project.getId();
        this.type = "PROJECT_" + action.toUpperCase();
        this.action = action + " project";
        this.content = project.getName();
        this.createdAt = project.getUpdatedAt() != null ? project.getUpdatedAt() : project.getCreatedAt();
        this.user = new UserSummaryDto(user);
        this.entity = new EntitySummaryDto(project.getId(), "PROJECT");
    }

    public ActivityResponseDto(Task task, String action, User user) {
        this.id = task.getId();
        this.type = "TASK_" + action.toUpperCase();
        this.action = action + " task";
        this.content = task.getTitle();
        this.createdAt = task.getUpdatedAt() != null ? task.getUpdatedAt() : task.getCreatedAt();
        this.user = new UserSummaryDto(user);
        this.entity = new EntitySummaryDto(task.getId(), "TASK");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UserSummaryDto getUser() { return user; }
    public void setUser(UserSummaryDto user) { this.user = user; }
    public EntitySummaryDto getEntity() { return entity; }
    public void setEntity(EntitySummaryDto entity) { this.entity = entity; }

    public static class UserSummaryDto {
        private Long id;
        private String name;
        private String firstName;
        private String lastName;
        private String profilePictureUrl;
        private String profilePictureFileName;

        public UserSummaryDto() {}

        public UserSummaryDto(User user) {
            this.id = user.getId();
            this.name = user.getFirstName() + " " + user.getLastName();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.profilePictureUrl = user.getProfilePictureUrl();
            this.profilePictureFileName = user.getProfilePictureFileName();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
        public String getProfilePictureFileName() { return profilePictureFileName; }
        public void setProfilePictureFileName(String profilePictureFileName) { this.profilePictureFileName = profilePictureFileName; }
    }

    public static class EntitySummaryDto {
        private Long id;
        private String type;

        public EntitySummaryDto() {}
        public EntitySummaryDto(Long id, String type) {
            this.id = id;
            this.type = type;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}