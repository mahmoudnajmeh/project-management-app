package com.example.project_management_app.service;

import com.example.project_management_app.dto.ProjectDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

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

            List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

            String notificationContent = currentUser.getFirstName() + " created project: " + project.getName();
            notificationService.createNotificationForUsers(
                    teamMembers,
                    notificationContent,
                    "PROJECT_CREATED",
                    savedProject.getId(),
                    "PROJECT",
                    currentUser.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName()
            );

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

            List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

            String notificationContent = currentUser.getFirstName() + " updated project: " + project.getName();
            notificationService.createNotificationForUsers(
                    teamMembers,
                    notificationContent,
                    "PROJECT_UPDATED",
                    project.getId(),
                    "PROJECT",
                    currentUser.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName()
            );

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

            List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

            String notificationContent = currentUser.getFirstName() + " deleted project: " + project.getName();
            notificationService.createNotificationForUsers(
                    teamMembers,
                    notificationContent,
                    "PROJECT_DELETED",
                    project.getId(),
                    "PROJECT",
                    currentUser.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName()
            );

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

            List<Project> createdProjects = projectRepository.findByCreatedBy(currentUser);
            logger.info("Found {} projects created by user {}", createdProjects.size(), currentUser.getUsername());

            List<Project> assignedProjects = projectRepository.findByTasksAssignedUser(currentUser);
            logger.info("Found {} projects assigned to user {}", assignedProjects.size(), currentUser.getUsername());

            List<Project> teamProjects = new ArrayList<>();

            List<Team> userTeams = teamRepository.findByMemberId(currentUser.getId());
            for (Team team : userTeams) {
                List<Project> teamProjectsList = projectRepository.findByTeam(team);
                teamProjects.addAll(teamProjectsList);
            }
            logger.info("Found {} projects from user's teams", teamProjects.size());

            Set<Project> allProjects = new HashSet<>(createdProjects);
            allProjects.addAll(assignedProjects);
            allProjects.addAll(teamProjects);

            List<Project> result = new ArrayList<>(allProjects);
            logger.info("Total unique projects for user {}: {}", currentUser.getUsername(), result.size());

            return result;
        } catch (Exception e) {
            logger.error("Error getting projects by user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user projects: " + e.getMessage());
        }
    }

    @Override
    public List<Project> searchProjects(String name) {
        try {
            User currentUser = accountService.getCurrentUser();

            List<Project> createdProjects = projectRepository.findByCreatedBy(currentUser);
            List<Project> assignedProjects = projectRepository.findByTasksAssignedUser(currentUser);

            Set<Project> allProjects = new HashSet<>(createdProjects);
            allProjects.addAll(assignedProjects);

            List<Project> filteredProjects = allProjects.stream()
                    .filter(project -> project.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();

            return filteredProjects;
        } catch (Exception e) {
            logger.error("Error searching projects: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search projects: " + e.getMessage());
        }
    }
}