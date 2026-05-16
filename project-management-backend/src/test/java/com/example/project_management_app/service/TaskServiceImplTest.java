package com.example.project_management_app.service;

import com.example.project_management_app.dto.TaskDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Captor
    private ArgumentCaptor<String> notificationContentCaptor;

    @Captor
    private ArgumentCaptor<String> notificationTypeCaptor;

    private User currentUser;
    private User assignedUser;
    private User anotherUser;
    private User teamLead;
    private Project project;
    private Task task1;
    private Task task2;
    private Task taskInProgress;
    private TaskDto taskDto;
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

        assignedUser = new User();
        assignedUser.setId(1L);
        assignedUser.setUsername("Mahmoud");
        assignedUser.setFirstName("Mahmoud");
        assignedUser.setLastName("Najmeh");

        anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("Katya");
        anotherUser.setFirstName("Katya");
        anotherUser.setLastName("Otto");

        teamLead = new User();
        teamLead.setId(3L);
        teamLead.setUsername("TeamLead");
        teamLead.setFirstName("Team");
        teamLead.setLastName("Lead");

        team = new Team();
        team.setId(1L);
        team.setName("Development Team");
        team.setCreatedBy(teamLead);

        teamMembers = Arrays.asList(currentUser, anotherUser, teamLead);

        project = new Project();
        project.setId(2L);
        project.setName("MN ChatBot");
        project.setCreatedBy(currentUser);
        project.setTeam(team);

        task1 = new Task();
        task1.setId(2L);
        task1.setTitle("Planning for MN ChatBot");
        task1.setDescription("MN ChatBot");
        task1.setStatus(Task.TaskStatus.DONE);
        task1.setPriority(Task.Priority.MEDIUM);
        task1.setDueDate(LocalDateTime.parse("2025-12-23T23:59:59"));
        task1.setProject(project);
        task1.setAssignedUser(assignedUser);

        taskInProgress = new Task();
        taskInProgress.setId(3L);
        taskInProgress.setTitle("Task In Progress");
        taskInProgress.setDescription("In Progress Description");
        taskInProgress.setStatus(Task.TaskStatus.IN_PROGRESS);
        taskInProgress.setPriority(Task.Priority.HIGH);
        taskInProgress.setDueDate(LocalDateTime.parse("2026-01-15T23:59:59"));
        taskInProgress.setProject(project);
        taskInProgress.setAssignedUser(assignedUser);

        task2 = new Task();
        task2.setId(6L);
        task2.setTitle("Authentication and Authorization");
        task2.setDescription("MN ChatBot");
        task2.setStatus(Task.TaskStatus.TODO);
        task2.setPriority(Task.Priority.HIGH);
        task2.setDueDate(LocalDateTime.parse("2026-03-02T23:59:59"));
        task2.setProject(project);
        task2.setAssignedUser(assignedUser);

        taskDto = new TaskDto();
        taskDto.setTitle("New Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(Task.TaskStatus.TODO);
        taskDto.setPriority(Task.Priority.HIGH);
        taskDto.setDueDate("2026-12-31T23:59:59");
        taskDto.setProjectId(2L);
        taskDto.setAssignedUserId(1L);
    }

    @Test
    void createTaskSuccess() {
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(assignedUser));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task1);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task createdTask = taskService.createTask(taskDto);

        assertNotNull(createdTask);
        assertEquals(2L, createdTask.getId());
        assertEquals("Planning for MN ChatBot", createdTask.getTitle());
        assertEquals(assignedUser, createdTask.getAssignedUser());
        assertEquals(project, createdTask.getProject());

        verify(projectRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(1L);
        verify(accountService, times(1)).getCurrentUser();
        verify(userRepository, times(1)).save(assignedUser);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(userService, times(1)).getUsersInSameTeam(currentUser);
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud created task: New Task in project: MN ChatBot"), eq("TASK_CREATED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void createTaskWithDefaultValues() {
        TaskDto minimalDto = new TaskDto();
        minimalDto.setTitle("Minimal Task");
        minimalDto.setProjectId(2L);
        minimalDto.setAssignedUserId(1L);

        Task newTask = new Task();
        newTask.setId(7L);
        newTask.setTitle("Minimal Task");
        newTask.setStatus(Task.TaskStatus.TODO);
        newTask.setPriority(Task.Priority.MEDIUM);
        newTask.setProject(project);
        newTask.setAssignedUser(assignedUser);

        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(assignedUser));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);
        when(userService.getUsersInSameTeam(currentUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task createdTask = taskService.createTask(minimalDto);

        assertNotNull(createdTask);
        assertEquals(7L, createdTask.getId());
        assertEquals("Minimal Task", createdTask.getTitle());
        assertEquals(Task.TaskStatus.TODO, createdTask.getStatus());
        assertEquals(Task.Priority.MEDIUM, createdTask.getPriority());

        verify(projectRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud created task: Minimal Task in project: MN ChatBot"), eq("TASK_CREATED"), eq(7L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void createTaskProjectNotFound() {
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(taskDto);
        });

        assertEquals("Project not found", exception.getMessage());
        verify(projectRepository, times(1)).findById(2L);
        verify(userRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTaskUserNotFound() {
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(taskDto);
        });

        assertEquals("User not found", exception.getMessage());
        verify(projectRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTaskThrowsException() {
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(assignedUser));
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(taskDto);
        });

        assertEquals("Database error", exception.getMessage());
        verify(projectRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, never()).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void updateTaskSuccessAsAssignee() {
        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Task Title");
        updateDto.setDescription("Updated Description");
        updateDto.setStatus(Task.TaskStatus.IN_PROGRESS);
        updateDto.setPriority(Task.Priority.HIGH);
        updateDto.setDueDate("2026-01-01T23:59:59");
        updateDto.setProjectId(2L);
        updateDto.setAssignedUserId(1L);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task1);
        when(userService.getUsersInSameTeam(assignedUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task updatedTask = taskService.updateTask(2L, updateDto);

        assertNotNull(updatedTask);
        assertEquals(2L, updatedTask.getId());

        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud updated task: Updated Task Title in project: MN ChatBot"), eq("TASK_UPDATED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void updateTaskSuccessAsProjectCreator() {
        User projectCreator = new User();
        projectCreator.setId(1L);
        projectCreator.setFirstName("Mahmoud");
        projectCreator.setLastName("Najmeh");

        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Task Title");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(projectCreator);
        when(userRepository.save(any(User.class))).thenReturn(projectCreator);
        when(taskRepository.save(any(Task.class))).thenReturn(task1);
        when(userService.getUsersInSameTeam(projectCreator)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task updatedTask = taskService.updateTask(2L, updateDto);

        assertNotNull(updatedTask);
        assertEquals(2L, updatedTask.getId());

        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud updated task: Updated Task Title in project: MN ChatBot"), eq("TASK_UPDATED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void updateTaskSuccessAsTeamLead() {
        User teamLead = new User();
        teamLead.setId(3L);
        teamLead.setFirstName("Team");
        teamLead.setLastName("Lead");

        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Task Title");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(teamLead);
        when(userRepository.save(any(User.class))).thenReturn(teamLead);
        when(taskRepository.save(any(Task.class))).thenReturn(task1);
        when(userService.getUsersInSameTeam(teamLead)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task updatedTask = taskService.updateTask(2L, updateDto);

        assertNotNull(updatedTask);
        assertEquals(2L, updatedTask.getId());

        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Team updated task: Updated Task Title in project: MN ChatBot"), eq("TASK_UPDATED"), eq(2L), eq("TASK"), eq(3L), eq("Team Lead"));
    }

    @Test
    void updateTaskWithStatusChangedToDone() {
        Task taskToComplete = new Task();
        taskToComplete.setId(4L);
        taskToComplete.setTitle("Task to Complete");
        taskToComplete.setDescription("To be completed");
        taskToComplete.setStatus(Task.TaskStatus.IN_PROGRESS);
        taskToComplete.setPriority(Task.Priority.HIGH);
        taskToComplete.setProject(project);
        taskToComplete.setAssignedUser(assignedUser);

        TaskDto statusChangeDto = new TaskDto();
        statusChangeDto.setStatus(Task.TaskStatus.DONE);

        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskToComplete));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenReturn(taskToComplete);
        when(userService.getUsersInSameTeam(assignedUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task updatedTask = taskService.updateTask(4L, statusChangeDto);

        assertNotNull(updatedTask);
        assertEquals(4L, updatedTask.getId());

        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud completed task: Task to Complete in project: MN ChatBot"), eq("TASK_COMPLETED"), eq(4L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void updateTaskWithAssignedUserChanged() {
        User newUser = new User();
        newUser.setId(4L);
        newUser.setUsername("NewUser");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        TaskDto reassignDto = new TaskDto();
        reassignDto.setAssignedUserId(4L);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.findById(4L)).thenReturn(Optional.of(newUser));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task1);
        when(userService.getUsersInSameTeam(assignedUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task updatedTask = taskService.updateTask(2L, reassignDto);

        assertNotNull(updatedTask);
        assertEquals(2L, updatedTask.getId());

        verify(userRepository, times(1)).findById(4L);
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud reassigned task: Planning for MN ChatBot in project: MN ChatBot"), eq("TASK_REASSIGNED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void updateTaskWithPartialData() {
        TaskDto partialDto = new TaskDto();
        partialDto.setTitle("Updated Title Only");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task1);
        when(userService.getUsersInSameTeam(assignedUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());

        Task updatedTask = taskService.updateTask(2L, partialDto);

        assertNotNull(updatedTask);
        assertEquals(2L, updatedTask.getId());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud updated task: Updated Title Only in project: MN ChatBot"), eq("TASK_UPDATED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
    }

    @Test
    void updateTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(99L, taskDto);
        });

        assertEquals("Task not found with id: 99", exception.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(accountService, never()).getCurrentUser();
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskUnauthorized() {
        User unauthorizedUser = new User();
        unauthorizedUser.setId(99L);
        unauthorizedUser.setUsername("Unauthorized");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(unauthorizedUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(2L, taskDto);
        });

        assertEquals("You are not authorized to update this task", exception.getMessage());
        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskAssignedUserNotFound() {
        TaskDto reassignDto = new TaskDto();
        reassignDto.setAssignedUserId(99L);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(2L, reassignDto);
        });

        assertEquals("User not found", exception.getMessage());
        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(userRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskThrowsException() {
        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Title");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTask(2L, updateDto);
        });

        assertEquals("Database error", exception.getMessage());
        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void deleteTaskSuccessAsAssignee() {
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(userService.getUsersInSameTeam(assignedUser)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
        doNothing().when(taskRepository).deleteById(2L);

        assertDoesNotThrow(() -> taskService.deleteTask(2L));

        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(userRepository, times(1)).save(assignedUser);
        verify(userService, times(1)).getUsersInSameTeam(assignedUser);
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud deleted task: Planning for MN ChatBot from project: MN ChatBot"), eq("TASK_DELETED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
        verify(taskRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteTaskSuccessAsProjectCreator() {
        User projectCreator = new User();
        projectCreator.setId(1L);
        projectCreator.setFirstName("Mahmoud");
        projectCreator.setLastName("Najmeh");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(projectCreator);
        when(userRepository.save(any(User.class))).thenReturn(projectCreator);
        when(userService.getUsersInSameTeam(projectCreator)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
        doNothing().when(taskRepository).deleteById(2L);

        assertDoesNotThrow(() -> taskService.deleteTask(2L));

        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Mahmoud deleted task: Planning for MN ChatBot from project: MN ChatBot"), eq("TASK_DELETED"), eq(2L), eq("TASK"), eq(1L), eq("Mahmoud Najmeh"));
        verify(taskRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteTaskSuccessAsTeamLead() {
        User teamLead = new User();
        teamLead.setId(3L);
        teamLead.setFirstName("Team");
        teamLead.setLastName("Lead");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(teamLead);
        when(userRepository.save(any(User.class))).thenReturn(teamLead);
        when(userService.getUsersInSameTeam(teamLead)).thenReturn(teamMembers);
        doNothing().when(notificationService).createNotificationForUsers(anyList(), anyString(), anyString(), anyLong(), anyString(), anyLong(), anyString());
        doNothing().when(taskRepository).deleteById(2L);

        assertDoesNotThrow(() -> taskService.deleteTask(2L));

        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(notificationService, times(1)).createNotificationForUsers(eq(teamMembers), eq("Team deleted task: Planning for MN ChatBot from project: MN ChatBot"), eq("TASK_DELETED"), eq(2L), eq("TASK"), eq(3L), eq("Team Lead"));
        verify(taskRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(99L);
        });

        assertEquals("Task not found with id: 99", exception.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(accountService, never()).getCurrentUser();
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTaskUnauthorized() {
        User unauthorizedUser = new User();
        unauthorizedUser.setId(99L);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(unauthorizedUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(2L);
        });

        assertEquals("You are not authorized to delete this task", exception.getMessage());
        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTaskThrowsException() {
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        doThrow(new RuntimeException("Database error")).when(taskRepository).deleteById(2L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(2L);
        });

        assertEquals("Database error", exception.getMessage());
        verify(taskRepository, times(1)).findById(2L);
        verify(accountService, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).deleteById(2L);
    }

    @Test
    void getTaskByIdSuccess() {
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task1));

        Task foundTask = taskService.getTaskById(2L);

        assertNotNull(foundTask);
        assertEquals(2L, foundTask.getId());
        assertEquals("Planning for MN ChatBot", foundTask.getTitle());

        verify(taskRepository, times(1)).findById(2L);
    }

    @Test
    void getTaskByIdNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.getTaskById(99L);
        });

        assertEquals("Task not found with id: 99", exception.getMessage());
        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    void getAllTasksSuccess() {
        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(6L, result.get(1).getId());

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getAllTasksEmpty() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTasksByProjectSuccess() {
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProject(project)).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.getTasksByProject(2L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(6L, result.get(1).getId());

        verify(projectRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).findByProject(project);
    }

    @Test
    void getTasksByProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.getTasksByProject(99L);
        });

        assertEquals("Project not found", exception.getMessage());
        verify(projectRepository, times(1)).findById(99L);
        verify(taskRepository, never()).findByProject(any());
    }

    @Test
    void getTasksByProjectEmpty() {
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProject(project)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getTasksByProject(2L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(projectRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).findByProject(project);
    }

    @Test
    void getTasksByUserSuccess() {
        List<Task> assignedTasks = Arrays.asList(task1, task2);
        List<Project> createdProjects = Arrays.asList(project);
        List<Task> projectTasks = Arrays.asList(task1, task2);
        List<Team> userTeams = Arrays.asList(team);
        List<Project> teamProjects = Arrays.asList(project);
        List<Project> projectsWithAssignedTasks = Arrays.asList(project);

        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.findByAssignedUser(assignedUser)).thenReturn(assignedTasks);
        when(projectRepository.findByCreatedBy(assignedUser)).thenReturn(createdProjects);
        when(taskRepository.findByProject(project)).thenReturn(projectTasks);
        when(teamRepository.findByMemberId(assignedUser.getId())).thenReturn(userTeams);
        when(projectRepository.findByTeam(team)).thenReturn(teamProjects);
        when(projectRepository.findByTasksAssignedUser(assignedUser)).thenReturn(projectsWithAssignedTasks);

        List<Task> result = taskService.getTasksByUser();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(accountService, times(1)).getCurrentUser();
        verify(userRepository, times(1)).save(assignedUser);
        verify(taskRepository, times(1)).findByAssignedUser(assignedUser);
        verify(projectRepository, times(1)).findByCreatedBy(assignedUser);
        verify(taskRepository, times(3)).findByProject(project);
        verify(teamRepository, times(1)).findByMemberId(assignedUser.getId());
        verify(projectRepository, times(1)).findByTeam(team);
        verify(projectRepository, times(1)).findByTasksAssignedUser(assignedUser);
    }

    @Test
    void getTasksByUserNoTeams() {
        List<Task> assignedTasks = Arrays.asList(task1, task2);
        List<Project> createdProjects = Arrays.asList(project);
        List<Task> projectTasks = Arrays.asList(task1, task2);

        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.findByAssignedUser(assignedUser)).thenReturn(assignedTasks);
        when(projectRepository.findByCreatedBy(assignedUser)).thenReturn(createdProjects);
        when(taskRepository.findByProject(project)).thenReturn(projectTasks);
        when(teamRepository.findByMemberId(assignedUser.getId())).thenReturn(Collections.emptyList());
        when(projectRepository.findByTasksAssignedUser(assignedUser)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getTasksByUser();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(teamRepository, times(1)).findByMemberId(assignedUser.getId());
        verify(projectRepository, never()).findByTeam(any());
    }

    @Test
    void getTasksByUserNoProjects() {
        when(accountService.getCurrentUser()).thenReturn(assignedUser);
        when(userRepository.save(any(User.class))).thenReturn(assignedUser);
        when(taskRepository.findByAssignedUser(assignedUser)).thenReturn(Collections.emptyList());
        when(projectRepository.findByCreatedBy(assignedUser)).thenReturn(Collections.emptyList());
        when(teamRepository.findByMemberId(assignedUser.getId())).thenReturn(Collections.emptyList());
        when(projectRepository.findByTasksAssignedUser(assignedUser)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getTasksByUser();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findByAssignedUser(assignedUser);
        verify(projectRepository, times(1)).findByCreatedBy(assignedUser);
        verify(teamRepository, times(1)).findByMemberId(assignedUser.getId());
        verify(projectRepository, times(1)).findByTasksAssignedUser(assignedUser);
    }

    @Test
    void getTasksByAssignedUserSuccess() {
        when(taskRepository.findByAssignedUser(assignedUser)).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.getTasksByAssignedUser(assignedUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(6L, result.get(1).getId());

        verify(taskRepository, times(1)).findByAssignedUser(assignedUser);
    }

    @Test
    void getTasksByAssignedUserEmpty() {
        when(taskRepository.findByAssignedUser(assignedUser)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getTasksByAssignedUser(assignedUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findByAssignedUser(assignedUser);
    }
}