package com.example.project_management_app.controller;

import com.example.project_management_app.dto.ActivityResponseDto;
import com.example.project_management_app.entity.Notification;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.ProjectService;
import com.example.project_management_app.service.TaskService;
import com.example.project_management_app.service.NotificationService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

    @Mock
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityController activityController;

    private ObjectMapper objectMapper;
    private User currentUser;
    private User senderUser;
    private Notification notification1;
    private Notification notification2;
    private Project project1;
    private Project project2;
    private Task task1;
    private Task task2;
    private List<Project> projects;
    private List<Task> tasks;
    private List<Notification> notifications;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(activityController).build();
        objectMapper = new ObjectMapper();

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("Mahmoud");
        currentUser.setEmail("mn.de@outlook.com");
        currentUser.setFirstName("Mahmoud");
        currentUser.setLastName("Najmeh");

        senderUser = new User();
        senderUser.setId(2L);
        senderUser.setUsername("Katya");
        senderUser.setFirstName("Katya");
        senderUser.setLastName("Otto");

        notification1 = new Notification();
        notification1.setId(1L);
        notification1.setType("TASK_ASSIGNED");
        notification1.setContent("Task assigned to you");
        notification1.setSenderId(2L);
        notification1.setCreatedAt(LocalDateTime.now().minusHours(2));
        notification1.setRead(false);

        notification2 = new Notification();
        notification2.setId(2L);
        notification2.setType("PROJECT_CREATED");
        notification2.setContent("New project created");
        notification2.setSenderId(2L);
        notification2.setCreatedAt(LocalDateTime.now().minusDays(1));
        notification2.setRead(true);

        notifications = Arrays.asList(notification1, notification2);

        project1 = new Project();
        project1.setId(1L);
        project1.setName("MN ChatBot");
        project1.setDescription("AI Chatbot project");
        project1.setCreatedAt(LocalDateTime.now().minusDays(5));
        project1.setUpdatedAt(LocalDateTime.now().minusDays(2));
        project1.setCreatedBy(currentUser);

        project2 = new Project();
        project2.setId(2L);
        project2.setName("Mobile App");
        project2.setDescription("Mobile application");
        project2.setCreatedAt(LocalDateTime.now().minusDays(15));
        project2.setUpdatedAt(LocalDateTime.now().minusDays(10));
        project2.setCreatedBy(senderUser);

        projects = Arrays.asList(project1, project2);

        task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Implement login");
        task1.setDescription("Add OAuth2 login");
        task1.setCreatedAt(LocalDateTime.now().minusDays(3));
        task1.setUpdatedAt(LocalDateTime.now().minusDays(1));
        task1.setAssignedUser(currentUser);
        task1.setStatus(Task.TaskStatus.IN_PROGRESS);

        task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Fix bug");
        task2.setDescription("Fix authentication bug");
        task2.setCreatedAt(LocalDateTime.now().minusDays(10));
        task2.setUpdatedAt(LocalDateTime.now().minusDays(8));
        task2.setAssignedUser(currentUser);
        task2.setStatus(Task.TaskStatus.DONE);

        tasks = Arrays.asList(task1, task2);
    }

    @Test
    void testGetRecentActivity() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(notifications);
        when(userRepository.findById(2L)).thenReturn(Optional.of(senderUser));
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));

        verify(accountService, times(1)).getCurrentUser();
        verify(notificationService, times(1)).getUserNotifications();
        verify(projectService, times(1)).getProjectsByUser();
        verify(taskService, times(1)).getTasksByUser();
    }

    @Test
    void testGetRecentActivityWithLimit() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(notifications);
        when(userRepository.findById(2L)).thenReturn(Optional.of(senderUser));
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent/limit/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(accountService, times(1)).getCurrentUser();
    }

    @Test
    void testGetRecentActivityWithLimitZero() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(notifications);
        when(userRepository.findById(2L)).thenReturn(Optional.of(senderUser));
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent/limit/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetRecentActivityWithLimitGreaterThanActivities() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(notifications);
        when(userRepository.findById(2L)).thenReturn(Optional.of(senderUser));
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent/limit/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void testGetRecentActivityNoNotifications() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(Arrays.asList());
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void testGetRecentActivityNoProjects() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(notifications);
        when(userRepository.findById(2L)).thenReturn(Optional.of(senderUser));
        when(projectService.getProjectsByUser()).thenReturn(Arrays.asList());
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void testGetRecentActivityNoTasks() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(notifications);
        when(userRepository.findById(2L)).thenReturn(Optional.of(senderUser));
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/activity/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void testGetRecentActivityEmptyEverything() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(Arrays.asList());
        when(projectService.getProjectsByUser()).thenReturn(Arrays.asList());
        when(taskService.getTasksByUser()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/activity/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetRecentActivityWithNullSenderId() throws Exception {
        Notification notificationWithoutSender = new Notification();
        notificationWithoutSender.setId(3L);
        notificationWithoutSender.setType("SYSTEM");
        notificationWithoutSender.setContent("System notification");
        notificationWithoutSender.setSenderId(null);
        notificationWithoutSender.setCreatedAt(LocalDateTime.now());
        notificationWithoutSender.setRead(false);

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(notificationService.getUserNotifications()).thenReturn(Arrays.asList(notificationWithoutSender));
        when(projectService.getProjectsByUser()).thenReturn(projects);
        when(taskService.getTasksByUser()).thenReturn(tasks);

        mockMvc.perform(get("/api/activity/recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }
}