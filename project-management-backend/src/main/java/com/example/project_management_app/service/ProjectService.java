package com.example.project_management_app.service;


import com.example.project_management_app.dto.ProjectDto;
import com.example.project_management_app.entity.Project;

import java.util.List;

public interface ProjectService {
    Project createProject(ProjectDto projectDto);
    Project updateProject(Long id, ProjectDto projectDto);
    void deleteProject(Long id);
    Project getProjectById(Long id);
    List<Project> getAllProjects();
    List<Project> getProjectsByUser();
    List<Project> searchProjects(String name);
}