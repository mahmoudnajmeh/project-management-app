package com.example.project_management_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teams")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> members;

    private LocalDateTime createdAt;

    // Team photo fields
    private String teamPhotoFileName;
    private String teamPhotoContentType;
    private Long teamPhotoSize;
    private String teamPhotoPath;
    private String teamPhotoUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Team() {}

    public Team(String name, String description, User createdBy) {
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
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTeamPhotoFileName() { return teamPhotoFileName; }
    public void setTeamPhotoFileName(String teamPhotoFileName) { this.teamPhotoFileName = teamPhotoFileName; }
    public String getTeamPhotoContentType() { return teamPhotoContentType; }
    public void setTeamPhotoContentType(String teamPhotoContentType) { this.teamPhotoContentType = teamPhotoContentType; }
    public Long getTeamPhotoSize() { return teamPhotoSize; }
    public void setTeamPhotoSize(Long teamPhotoSize) { this.teamPhotoSize = teamPhotoSize; }
    public String getTeamPhotoPath() { return teamPhotoPath; }
    public void setTeamPhotoPath(String teamPhotoPath) { this.teamPhotoPath = teamPhotoPath; }
    public String getTeamPhotoUrl() { return teamPhotoUrl; }
    public void setTeamPhotoUrl(String teamPhotoUrl) { this.teamPhotoUrl = teamPhotoUrl; }
}