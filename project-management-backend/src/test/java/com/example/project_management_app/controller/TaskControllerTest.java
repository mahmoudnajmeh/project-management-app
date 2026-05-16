package com.example.project_management_app.controller;

import com.example.project_management_app.dto.TaskDto;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.TaskService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;
    private Task task1;
    private Task task2;
    private TaskDto taskDto;
    private List<Task> taskList;
    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();

        project = new Project();
        project.setId(2L);
        project.setName("MN ChatBot");

        user = new User();
        user.setId(1L);
        user.setUsername("Mahmoud");

        task1 = new Task();
        task1.setId(2L);
        task1.setTitle("Planning for MN ChatBot");
        task1.setDescription("MN ChatBot");
        task1.setStatus(Task.TaskStatus.DONE);
        task1.setPriority(Task.Priority.MEDIUM);
        task1.setDueDate(LocalDateTime.parse("2025-12-23T23:59:59"));
        task1.setProject(project);
        task1.setAssignedUser(user);

        task2 = new Task();
        task2.setId(6L);
        task2.setTitle("Authentication and Authorization");
        task2.setDescription("MN ChatBot");
        task2.setStatus(Task.TaskStatus.TODO);
        task2.setPriority(Task.Priority.HIGH);
        task2.setDueDate(LocalDateTime.parse("2026-03-02T23:59:59"));
        task2.setProject(project);
        task2.setAssignedUser(user);

        taskList = Arrays.asList(task1, task2);

        taskDto = new TaskDto();
        taskDto.setTitle("New Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(Task.TaskStatus.TODO);
        taskDto.setPriority(Task.Priority.HIGH);
        taskDto.setDueDate("2026-12-31T23:59:59");
        taskDto.setProjectId(1L);
        taskDto.setAssignedUserId(1L);
    }

    @Test
    void getAllTasks() throws Exception {
        when(taskService.getAllTasks()).thenReturn(taskList);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Planning for MN ChatBot"))
                .andExpect(jsonPath("$[1].id").value(6))
                .andExpect(jsonPath("$[1].title").value("Authentication and Authorization"));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void getMyTasks() throws Exception {
        when(taskService.getTasksByUser()).thenReturn(taskList);

        mockMvc.perform(get("/api/tasks/my-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assignedUser.id").value(1))
                .andExpect(jsonPath("$[1].assignedUser.id").value(1));

        verify(taskService, times(1)).getTasksByUser();
    }

    @Test
    void getTasksByProject() throws Exception {
        Long projectId = 2L;
        when(taskService.getTasksByProject(projectId)).thenReturn(taskList);

        mockMvc.perform(get("/api/tasks/project/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].project.id").value(2))
                .andExpect(jsonPath("$[1].project.id").value(2));

        verify(taskService, times(1)).getTasksByProject(projectId);
    }

    @Test
    void getTaskById() throws Exception {
        Long taskId = 2L;
        when(taskService.getTaskById(taskId)).thenReturn(task1);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Planning for MN ChatBot"));

        verify(taskService, times(1)).getTaskById(taskId);
    }

    @Test
    void createTask() throws Exception {
        when(taskService.createTask(any(TaskDto.class))).thenReturn(task1);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Planning for MN ChatBot"));

        verify(taskService, times(1)).createTask(any(TaskDto.class));
    }

    @Test
    void updateTaskSuccess() throws Exception {
        Long taskId = 2L;
        when(taskService.updateTask(eq(taskId), any(TaskDto.class))).thenReturn(task1);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(taskService, times(1)).updateTask(eq(taskId), any(TaskDto.class));
    }

    @Test
    void updateTaskForbidden() throws Exception {
        Long taskId = 2L;
        when(taskService.updateTask(eq(taskId), any(TaskDto.class)))
                .thenThrow(new RuntimeException("Not authorized to update this task"));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not authorized to update this task"));
    }

    @Test
    void updateTaskRuntimeError() throws Exception {
        Long taskId = 2L;
        when(taskService.updateTask(eq(taskId), any(TaskDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Database error"));
    }

    @Test
    void deleteTaskSuccess() throws Exception {
        Long taskId = 2L;
        doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isOk());

        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    void deleteTaskForbidden() throws Exception {
        Long taskId = 2L;
        doThrow(new RuntimeException("Not authorized to delete this task"))
                .when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not authorized to delete this task"));
    }

    @Test
    void deleteTaskRuntimeError() throws Exception {
        Long taskId = 2L;
        doThrow(new RuntimeException("Database error"))
                .when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Database error"));
    }
}