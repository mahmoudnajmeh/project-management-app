package com.example.project_management_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TaskStatus.TODO;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Task() {}

    public Task(String title, String description, Project project, User assignedUser) {
        this.title = title;
        this.description = description;
        this.project = project;
        this.assignedUser = assignedUser;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @JsonProperty("project")
    public ProjectJson getProjectJson() {
        if (project == null) return null;
        return new ProjectJson(project.getId(), project.getName());
    }

    @JsonProperty("assignedUser")
    public UserJson getAssignedUserJson() {
        if (assignedUser == null) return null;
        return new UserJson(
                assignedUser.getId(),
                assignedUser.getUsername(),
                assignedUser.getFirstName(),
                assignedUser.getLastName(),
                assignedUser.getEmail(),
                assignedUser.getProfilePictureFileName(),
                assignedUser.getProfilePictureUrl(),
                assignedUser.getProfilePicturePath(),
                assignedUser.getProfilePictureContentType()
        );
    }

    @JsonIgnore
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    @JsonIgnore
    public User getAssignedUser() { return assignedUser; }
    public void setAssignedUser(User assignedUser) { this.assignedUser = assignedUser; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectJson {
        private Long id;
        private String name;

        public ProjectJson(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserJson {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String profilePictureFileName;
        private String profilePictureUrl;
        private String profilePicturePath;
        private String profilePictureContentType;

        public UserJson(Long id, String username, String firstName, String lastName, String email,
                        String profilePictureFileName, String profilePictureUrl, String profilePicturePath, String profilePictureContentType) {
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.profilePictureFileName = profilePictureFileName;
            this.profilePictureUrl = profilePictureUrl;
            this.profilePicturePath = profilePicturePath;
            this.profilePictureContentType = profilePictureContentType;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getProfilePictureFileName() { return profilePictureFileName; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public String getProfilePicturePath() { return profilePicturePath; }
        public String getProfilePictureContentType() { return profilePictureContentType; }
    }

    public enum TaskStatus {
        TODO, IN_PROGRESS, REVIEW, DONE
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}