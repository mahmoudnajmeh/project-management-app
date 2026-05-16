package com.example.project_management_app.service;

import com.example.project_management_app.dto.ProjectDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User currentUser;
    private User anotherUser;
    private Project project;
    private ProjectDto projectDto;
    private Team team;
    private List<User> teamMembers;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("Mahmoud");
        currentUser.setFirstName("Mahmoud");
        currentUser.setLastName("Najmeh");
        currentUser.setEmail("mn.de@outlook.com");
        currentUser.setRole(User.Role.ROLE_USER);

        anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("Katya");
        anotherUser.setFirstName("Katya");
        anotherUser.setLastName("Otto");
        anotherUser.setEmail("mamocool3@gmail.com");
        anotherUser.setRole(User.Role.ROLE_USER);

        team = new Team();
        team.setId(1L);
        team.setName("Development Team");

        teamMembers = Arrays.asList(currentUser, anotherUser);

        project = new Project();
        project.setId(1L);
        project.setName("MN ChatBot");
        project.setDescription("MN ChatBot is a modern, intelligent conversational platform");
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        project.setStartDate(LocalDateTime.parse("2025-12-14T00:00:00"));
        project.setEndDate(LocalDateTime.parse("2026-06-01T23:59:59"));
        project.setCreatedBy(currentUser);
        project.setTeam(team);

        projectDto = new ProjectDto();
        projectDto.setName("Updated Project");
        projectDto.setDescription("Updated Description");
        projectDto.setStatus(Project.ProjectStatus.COMPLETED);
        projectDto.setStartDate("2026-01-01T00:00:00");
        projectDto.setEndDate("2026-12-31T23:59:59");
    }

    @Test
    void createProjectSuccess() {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Project createdProject = projectService.createProject(projectDto);

        assertNotNull(createdProject);
        assertEquals(1L, createdProject.getId());
        assertEquals("MN ChatBot", createdProject.getName());
        assertEquals(Project.ProjectStatus.IN_PROGRESS, createdProject.getStatus());
        assertEquals(currentUser, createdProject.getCreatedBy());

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(userService, times(1)).getUsersInSameTeam(currentUser);
        verify(notificationService, times(1)).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void createProjectWithNullDates() {
        ProjectDto dtoWithNullDates = new ProjectDto();
        dtoWithNullDates.setName("Test Project");
        dtoWithNullDates.setDescription("Test Description");
        dtoWithNullDates.setStartDate(null);
        dtoWithNullDates.setEndDate(null);

        Project testProject = new Project();
        testProject.setId(2L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setCreatedBy(currentUser);
        testProject.setStatus(Project.ProjectStatus.PLANNED);

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Project createdProject = projectService.createProject(dtoWithNullDates);

        assertNotNull(createdProject);
        assertEquals(2L, createdProject.getId());
        assertEquals("Test Project", createdProject.getName());
        assertNull(createdProject.getStartDate());
        assertNull(createdProject.getEndDate());
    }

    @Test
    void createProjectThrowsException() {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.save(any(Project.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.createProject(projectDto);
        });

        assertEquals("Failed to create project: Database error", exception.getMessage());
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(userService, never()).getUsersInSameTeam(any());
        verify(notificationService, never()).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void updateProjectSuccess() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Project updatedProject = projectService.updateProject(1L, projectDto);

        assertNotNull(updatedProject);
        assertEquals(1L, updatedProject.getId());
        assertEquals("Updated Project", updatedProject.getName());
        assertEquals("Updated Description", updatedProject.getDescription());
        assertEquals(Project.ProjectStatus.COMPLETED, updatedProject.getStatus());
        assertEquals(LocalDateTime.parse("2026-01-01T00:00:00"), updatedProject.getStartDate());
        assertEquals(LocalDateTime.parse("2026-12-31T23:59:59"), updatedProject.getEndDate());

        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(userService, times(1)).getUsersInSameTeam(currentUser);
        verify(notificationService, times(1)).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void updateProjectWithPartialData() {
        ProjectDto partialDto = new ProjectDto();
        partialDto.setName("Partially Updated");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Project updatedProject = projectService.updateProject(1L, partialDto);

        assertNotNull(updatedProject);
        assertEquals("Partially Updated", updatedProject.getName());
        assertEquals("MN ChatBot is a modern, intelligent conversational platform", updatedProject.getDescription());
        assertEquals(Project.ProjectStatus.IN_PROGRESS, updatedProject.getStatus());

        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void updateProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.updateProject(99L, projectDto);
        });

        assertEquals("Failed to update project: Project not found with id: 99", exception.getMessage());
        verify(projectRepository, times(1)).findById(99L);
        verify(accountService, never()).getCurrentUser();
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateProjectNotOwner() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(anotherUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.updateProject(1L, projectDto);
        });

        assertEquals("Failed to update project: You can only update your own projects", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateProjectThrowsException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.save(any(Project.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.updateProject(1L, projectDto);
        });

        assertEquals("Failed to update project: Database error", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void deleteProjectSuccess() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
        doNothing().when(projectRepository).deleteById(1L);

        assertDoesNotThrow(() -> projectService.deleteProject(1L));

        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(userService, times(1)).getUsersInSameTeam(currentUser);
        verify(notificationService, times(1)).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(99L);
        });

        assertEquals("Failed to delete project: Project not found with id: 99", exception.getMessage());
        verify(projectRepository, times(1)).findById(99L);
        verify(accountService, never()).getCurrentUser();
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProjectNotOwner() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(anotherUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(1L);
        });

        assertEquals("Failed to delete project: You can only delete your own projects", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProjectThrowsException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        doThrow(new RuntimeException("Database error")).when(projectRepository).deleteById(1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(1L);
        });

        assertEquals("Failed to delete project: Database error", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void getProjectByIdSuccess() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project foundProject = projectService.getProjectById(1L);

        assertNotNull(foundProject);
        assertEquals(1L, foundProject.getId());
        assertEquals("MN ChatBot", foundProject.getName());
        assertEquals(currentUser, foundProject.getCreatedBy());

        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void getProjectByIdNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.getProjectById(99L);
        });

        assertEquals("Project not found with id: 99", exception.getMessage());
        verify(projectRepository, times(1)).findById(99L);
    }

    @Test
    void getAllProjectsSuccess() {
        List<Project> projects = Arrays.asList(project);
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.getAllProjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("MN ChatBot", result.get(0).getName());

        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getAllProjectsEmpty() {
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        List<Project> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getAllProjectsThrowsException() {
        when(projectRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.getAllProjects();
        });

        assertEquals("Failed to get projects: Database error", exception.getMessage());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getProjectsByUserSuccess() {
        List<Project> createdProjects = Arrays.asList(project);
        List<Project> assignedProjects = Arrays.asList();
        List<Team> userTeams = Arrays.asList(team);
        List<Project> teamProjects = Arrays.asList(project);

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByCreatedBy(currentUser)).thenReturn(createdProjects);
        when(projectRepository.findByTasksAssignedUser(currentUser)).thenReturn(assignedProjects);
        when(teamRepository.findByMemberId(currentUser.getId())).thenReturn(userTeams);
        when(projectRepository.findByTeam(team)).thenReturn(teamProjects);

        List<Project> result = projectService.getProjectsByUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).findByCreatedBy(currentUser);
        verify(projectRepository, times(1)).findByTasksAssignedUser(currentUser);
        verify(teamRepository, times(1)).findByMemberId(currentUser.getId());
        verify(projectRepository, times(1)).findByTeam(team);
    }

    @Test
    void getProjectsByUserWithMultipleTeams() {
        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("QA Team");

        List<Project> createdProjects = Arrays.asList(project);
        List<Project> assignedProjects = Arrays.asList();
        List<Team> userTeams = Arrays.asList(team, team2);
        List<Project> teamProjects1 = Arrays.asList(project);
        List<Project> teamProjects2 = Arrays.asList();

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByCreatedBy(currentUser)).thenReturn(createdProjects);
        when(projectRepository.findByTasksAssignedUser(currentUser)).thenReturn(assignedProjects);
        when(teamRepository.findByMemberId(currentUser.getId())).thenReturn(userTeams);
        when(projectRepository.findByTeam(team)).thenReturn(teamProjects1);
        when(projectRepository.findByTeam(team2)).thenReturn(teamProjects2);

        List<Project> result = projectService.getProjectsByUser();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(currentUser.getId());
        verify(projectRepository, times(1)).findByTeam(team);
        verify(projectRepository, times(1)).findByTeam(team2);
    }

    @Test
    void getProjectsByUserNoTeams() {
        List<Project> createdProjects = Arrays.asList(project);
        List<Project> assignedProjects = Arrays.asList();

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByCreatedBy(currentUser)).thenReturn(createdProjects);
        when(projectRepository.findByTasksAssignedUser(currentUser)).thenReturn(assignedProjects);
        when(teamRepository.findByMemberId(currentUser.getId())).thenReturn(Collections.emptyList());

        List<Project> result = projectService.getProjectsByUser();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(currentUser.getId());
        verify(projectRepository, never()).findByTeam(any());
    }

    @Test
    void getProjectsByUserThrowsException() {
        when(accountService.getCurrentUser()).thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.getProjectsByUser();
        });

        assertEquals("Failed to get user projects: User not found", exception.getMessage());
        verify(accountService, times(1)).getCurrentUser();
    }

    @Test
    void searchProjectsSuccess() {
        List<Project> createdProjects = Arrays.asList(project);
        List<Project> assignedProjects = Arrays.asList();

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByCreatedBy(currentUser)).thenReturn(createdProjects);
        when(projectRepository.findByTasksAssignedUser(currentUser)).thenReturn(assignedProjects);

        List<Project> result = projectService.searchProjects("ChatBot");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MN ChatBot", result.get(0).getName());

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).findByCreatedBy(currentUser);
        verify(projectRepository, times(1)).findByTasksAssignedUser(currentUser);
    }

    @Test
    void searchProjectsNoMatches() {
        List<Project> createdProjects = Arrays.asList(project);
        List<Project> assignedProjects = Arrays.asList();

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByCreatedBy(currentUser)).thenReturn(createdProjects);
        when(projectRepository.findByTasksAssignedUser(currentUser)).thenReturn(assignedProjects);

        List<Project> result = projectService.searchProjects("Nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).findByCreatedBy(currentUser);
        verify(projectRepository, times(1)).findByTasksAssignedUser(currentUser);
    }

    @Test
    void searchProjectsWithAssignedProjects() {
        Project assignedProject = new Project();
        assignedProject.setId(3L);
        assignedProject.setName("Test Project");
        assignedProject.setDescription("Test Description");

        List<Project> createdProjects = Arrays.asList(project);
        List<Project> assignedProjects = Arrays.asList(assignedProject);

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByCreatedBy(currentUser)).thenReturn(createdProjects);
        when(projectRepository.findByTasksAssignedUser(currentUser)).thenReturn(assignedProjects);

        List<Project> result = projectService.searchProjects("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals("Test Project", result.get(0).getName());

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).findByCreatedBy(currentUser);
        verify(projectRepository, times(1)).findByTasksAssignedUser(currentUser);
    }

    @Test
    void searchProjectsThrowsException() {
        when(accountService.getCurrentUser()).thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.searchProjects("ChatBot");
        });

        assertEquals("Failed to search projects: User not found", exception.getMessage());
        verify(accountService, times(1)).getCurrentUser();
    }
}