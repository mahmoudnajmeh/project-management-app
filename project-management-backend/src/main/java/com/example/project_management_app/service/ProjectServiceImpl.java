package com.example.project_management_app.service;

import com.example.project_management_app.dto.ProjectDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AccountService accountService;

    @Override
    public Project createProject(ProjectDto projectDto) {
        try {
            User currentUser = accountService.getCurrentUser();
            logger.info("Creating project for user: {}", currentUser.getUsername());

            Project project = new Project();
            project.setName(projectDto.getName());
            project.setDescription(projectDto.getDescription());
            project.setCreatedBy(currentUser);
            project.setStatus(Project.ProjectStatus.PLANNED);

            if (projectDto.getStartDate() != null && !projectDto.getStartDate().isEmpty()) {
                LocalDateTime startDate = LocalDateTime.parse(projectDto.getStartDate());
                project.setStartDate(startDate);
            }

            if (projectDto.getEndDate() != null && !projectDto.getEndDate().isEmpty()) {
                LocalDateTime endDate = LocalDateTime.parse(projectDto.getEndDate());
                project.setEndDate(endDate);
            }

            Project savedProject = projectRepository.save(project);
            logger.info("Project created successfully with ID: {}", savedProject.getId());
            return savedProject;
        } catch (Exception e) {
            logger.error("Error creating project: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create project: " + e.getMessage());
        }
    }

    @Override
    public Project updateProject(Long id, ProjectDto projectDto) {
        try {
            Project project = getProjectById(id);
            User currentUser = accountService.getCurrentUser();

            if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You can only update your own projects");
            }

            if (projectDto.getName() != null) {
                project.setName(projectDto.getName());
            }
            if (projectDto.getDescription() != null) {
                project.setDescription(projectDto.getDescription());
            }
            if (projectDto.getStatus() != null) {
                project.setStatus(projectDto.getStatus());
            }
            if (projectDto.getStartDate() != null && !projectDto.getStartDate().isEmpty()) {
                LocalDateTime startDate = LocalDateTime.parse(projectDto.getStartDate());
                project.setStartDate(startDate);
            }
            if (projectDto.getEndDate() != null && !projectDto.getEndDate().isEmpty()) {
                LocalDateTime endDate = LocalDateTime.parse(projectDto.getEndDate());
                project.setEndDate(endDate);
            }

            Project updatedProject = projectRepository.save(project);
            logger.info("Project updated successfully: {}", updatedProject.getId());
            return updatedProject;
        } catch (Exception e) {
            logger.error("Error updating project {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update project: " + e.getMessage());
        }
    }

    @Override
    public void deleteProject(Long id) {
        try {
            Project project = getProjectById(id);
            User currentUser = accountService.getCurrentUser();

            if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You can only delete your own projects");
            }

            projectRepository.deleteById(id);
            logger.info("Project deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting project {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete project: " + e.getMessage());
        }
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    @Override
    public List<Project> getAllProjects() {
        try {
            List<Project> projects = projectRepository.findAll();
            logger.info("Retrieved {} projects from database", projects.size());
            return projects;
        } catch (Exception e) {
            logger.error("Error getting all projects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get projects: " + e.getMessage());
        }
    }

    @Override
    public List<Project> getProjectsByUser() {
        try {
            User currentUser = accountService.getCurrentUser();
            logger.info("Getting projects for user: {}", currentUser.getUsername());
            List<Project> projects = projectRepository.findByCreatedBy(currentUser);
            logger.info("Found {} projects for user {}", projects.size(), currentUser.getUsername());
            return projects;
        } catch (Exception e) {
            logger.error("Error getting projects by user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user projects: " + e.getMessage());
        }
    }

    @Override
    public List<Project> searchProjects(String name) {
        try {
            return projectRepository.findByNameContainingIgnoreCase(name);
        } catch (Exception e) {
            logger.error("Error searching projects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search projects: " + e.getMessage());
        }
    }
}