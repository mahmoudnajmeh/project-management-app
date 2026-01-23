package com.example.project_management_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Task> tasks;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ProjectStatus.PLANNED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Project() {}

    public Project(String name, String description, User createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @JsonProperty("createdBy")
    public UserJson getCreatedByJson() {
        if (createdBy == null) return null;
        return new UserJson(
                createdBy.getId(),
                createdBy.getUsername(),
                createdBy.getFirstName(),
                createdBy.getLastName(),
                createdBy.getEmail(),
                createdBy.getProfilePictureFileName(),
                createdBy.getProfilePictureUrl(),
                createdBy.getProfilePicturePath(),
                createdBy.getProfilePictureContentType()
        );
    }

    @JsonIgnore
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

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

    public enum ProjectStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}