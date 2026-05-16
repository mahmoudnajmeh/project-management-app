package com.example.project_management_app.controller;

import com.example.project_management_app.dto.ProjectDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ProjectController projectController;

    private ObjectMapper objectMapper;
    private Project project;
    private ProjectDto projectDto;
    private User currentUser;
    private List<Project> projectList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
        objectMapper = new ObjectMapper();

        currentUser = new User();
        currentUser.setId(2L);
        currentUser.setUsername("Mahmoud");

        User creator = new User();
        creator.setId(2L);
        creator.setUsername("Mahmoud");

        project = new Project();
        project.setId(1L);
        project.setName("MN ChatBot");
        project.setDescription("MN ChatBot is a modern, intelligent conversational platform");
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        project.setStartDate(LocalDateTime.parse("2025-12-14T00:00:00"));
        project.setEndDate(LocalDateTime.parse("2026-06-01T23:59:59"));
        project.setCreatedBy(creator);
        project.setCreatedAt(LocalDateTime.parse("2025-12-19T10:36:40.361474"));
        project.setUpdatedAt(LocalDateTime.parse("2026-01-28T09:22:34.919156"));

        // IMPORTANT: Initialize tasks to avoid NullPointerException
        project.setTasks(new ArrayList<>());

        projectList = Arrays.asList(project);

        projectDto = new ProjectDto();
        projectDto.setName("New Project");
        projectDto.setDescription("New Description");
        projectDto.setStatus(Project.ProjectStatus.PLANNED);
        projectDto.setStartDate("2026-01-01T00:00:00");
        projectDto.setEndDate("2026-12-31T23:59:59");
        projectDto.setCreatedBy(2L);
    }

    @Test
    void getProjectSuccess() throws Exception {
        Long projectId = 1L;

        when(projectService.getProjectById(projectId)).thenReturn(project);
        when(accountService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("MN ChatBot"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(projectService, times(1)).getProjectById(projectId);
        verify(accountService, times(1)).getCurrentUser();
    }

    @Test
    void getProjectAccessDenied() throws Exception {
        Long projectId = 1L;
        User differentUser = new User();
        differentUser.setId(3L); // Different from creator (2L)

        when(projectService.getProjectById(projectId)).thenReturn(project);
        when(accountService.getCurrentUser()).thenReturn(differentUser);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied"));
    }

    @Test
    void getProjectNotFound() throws Exception {
        Long projectId = 999L;
        when(projectService.getProjectById(projectId))
                .thenThrow(new RuntimeException("Project not found"));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Project not found"));
    }

    @Test
    void getProjectServerError() throws Exception {
        Long projectId = 1L;
        when(projectService.getProjectById(projectId))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error: Database connection failed"));
    }

    @Test
    void getMyProjects() throws Exception {
        when(projectService.getProjectsByUser()).thenReturn(projectList);

        mockMvc.perform(get("/api/projects/my-projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("MN ChatBot"));

        verify(projectService, times(1)).getProjectsByUser();
    }

    @Test
    void getMyProjectsError() throws Exception {
        when(projectService.getProjectsByUser())
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/projects/my-projects"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error"));
    }

    @Test
    void getAllProjects() throws Exception {
        when(projectService.getAllProjects()).thenReturn(projectList);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("MN ChatBot"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    void getAllProjectsError() throws Exception {
        when(projectService.getAllProjects())
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error"));
    }

    @Test
    void createProject() throws Exception {
        when(projectService.createProject(any(ProjectDto.class))).thenReturn(project);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("MN ChatBot"));

        verify(projectService, times(1)).createProject(any(ProjectDto.class));
    }

    @Test
    void createProjectError() throws Exception {
        when(projectService.createProject(any(ProjectDto.class)))
                .thenThrow(new RuntimeException("Validation failed"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error: Validation failed"));
    }

    @Test
    void updateProjectSuccess() throws Exception {
        Long projectId = 1L;
        when(projectService.updateProject(eq(projectId), any(ProjectDto.class))).thenReturn(project);

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(projectService, times(1)).updateProject(eq(projectId), any(ProjectDto.class));
    }

    @Test
    void updateProjectBadRequest() throws Exception {
        Long projectId = 1L;
        when(projectService.updateProject(eq(projectId), any(ProjectDto.class)))
                .thenThrow(new RuntimeException("Invalid project data"));

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid project data"));
    }

    @Test
    void updateProjectRuntimeError() throws Exception {
        Long projectId = 1L;
        when(projectService.updateProject(eq(projectId), any(ProjectDto.class)))
                .thenThrow(new NullPointerException("Null pointer"));

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Null pointer"));
    }

    @Test
    void deleteProjectSuccess() throws Exception {
        Long projectId = 1L;
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isOk());

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    void deleteProjectBadRequest() throws Exception {
        Long projectId = 1L;
        doThrow(new RuntimeException("Cannot delete project with tasks"))
                .when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot delete project with tasks"));
    }

    @Test
    void deleteProjectRuntimeError() throws Exception {
        Long projectId = 1L;
        doThrow(new NullPointerException("Null pointer"))
                .when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Null pointer"));
    }

    @Test
    void searchProjects() throws Exception {
        String searchTerm = "Chat";
        when(projectService.searchProjects(searchTerm)).thenReturn(projectList);

        mockMvc.perform(get("/api/projects/search")
                        .param("name", searchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("MN ChatBot"));

        verify(projectService, times(1)).searchProjects(searchTerm);
    }

    @Test
    void searchProjectsError() throws Exception {
        String searchTerm = "Chat";
        when(projectService.searchProjects(searchTerm))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/projects/search")
                        .param("name", searchTerm))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error"));
    }
}