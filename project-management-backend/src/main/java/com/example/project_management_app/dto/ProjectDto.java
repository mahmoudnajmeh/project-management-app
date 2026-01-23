package com.example.project_management_app.dto;

import com.example.project_management_app.entity.Project;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ProjectDto {
    private Long id;

    @NotBlank
    private String name;

    private String description;
    private Project.ProjectStatus status;
    private String startDate;
    private String endDate;
    private Long createdBy;

    public ProjectDto() {}

    public ProjectDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Project.ProjectStatus getStatus() { return status; }
    public void setStatus(Project.ProjectStatus status) { this.status = status; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}