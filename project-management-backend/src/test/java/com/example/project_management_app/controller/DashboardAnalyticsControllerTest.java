package com.example.project_management_app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DashboardAnalyticsController dashboardAnalyticsController;

    private Map<String, Object> tasksMetrics;
    private Map<String, Object> projectsMetrics;
    private Map<String, Object> usersMetrics;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardAnalyticsController).build();

        tasksMetrics = new HashMap<>();
        tasksMetrics.put("completed", 25);
        tasksMetrics.put("in_progress", 15);
        tasksMetrics.put("todo", 10);
        tasksMetrics.put("overdue", 3);

        projectsMetrics = new HashMap<>();
        projectsMetrics.put("total", 8);
        projectsMetrics.put("completed", 3);
        projectsMetrics.put("in_progress", 5);

        usersMetrics = new HashMap<>();
        usersMetrics.put("total", 12);
        usersMetrics.put("online", 5);
    }

    @Test
    void testGetDashboardSummarySuccess() throws Exception {
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(tasksMetrics, projectsMetrics, usersMetrics);

        mockMvc.perform(get("/api/dashboard/analytics/summary")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.completed").value(25))
                .andExpect(jsonPath("$.tasks.in_progress").value(15))
                .andExpect(jsonPath("$.tasks.todo").value(10))
                .andExpect(jsonPath("$.tasks.overdue").value(3))
                .andExpect(jsonPath("$.projects.total").value(8))
                .andExpect(jsonPath("$.projects.completed").value(3))
                .andExpect(jsonPath("$.projects.in_progress").value(5))
                .andExpect(jsonPath("$.users.total").value(12))
                .andExpect(jsonPath("$.users.online").value(5));

        verify(jdbcTemplate, times(3)).queryForMap(anyString());
    }

    @Test
    void testGetDashboardSummaryWithZeroValues() throws Exception {
        Map<String, Object> emptyTasks = new HashMap<>();
        emptyTasks.put("completed", 0);
        emptyTasks.put("in_progress", 0);
        emptyTasks.put("todo", 0);
        emptyTasks.put("overdue", 0);

        Map<String, Object> emptyProjects = new HashMap<>();
        emptyProjects.put("total", 0);
        emptyProjects.put("completed", 0);
        emptyProjects.put("in_progress", 0);

        Map<String, Object> emptyUsers = new HashMap<>();
        emptyUsers.put("total", 0);
        emptyUsers.put("online", 0);

        when(jdbcTemplate.queryForMap(anyString())).thenReturn(emptyTasks, emptyProjects, emptyUsers);

        mockMvc.perform(get("/api/dashboard/analytics/summary")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.completed").value(0))
                .andExpect(jsonPath("$.tasks.in_progress").value(0))
                .andExpect(jsonPath("$.tasks.todo").value(0))
                .andExpect(jsonPath("$.tasks.overdue").value(0))
                .andExpect(jsonPath("$.projects.total").value(0))
                .andExpect(jsonPath("$.projects.completed").value(0))
                .andExpect(jsonPath("$.projects.in_progress").value(0))
                .andExpect(jsonPath("$.users.total").value(0))
                .andExpect(jsonPath("$.users.online").value(0));

        verify(jdbcTemplate, times(3)).queryForMap(anyString());
    }

    @Test
    void testGetDashboardSummaryWithLargeNumbers() throws Exception {
        Map<String, Object> largeTasks = new HashMap<>();
        largeTasks.put("completed", 9999);
        largeTasks.put("in_progress", 8888);
        largeTasks.put("todo", 7777);
        largeTasks.put("overdue", 6666);

        Map<String, Object> largeProjects = new HashMap<>();
        largeProjects.put("total", 555);
        largeProjects.put("completed", 444);
        largeProjects.put("in_progress", 111);

        Map<String, Object> largeUsers = new HashMap<>();
        largeUsers.put("total", 1000);
        largeUsers.put("online", 500);

        when(jdbcTemplate.queryForMap(anyString())).thenReturn(largeTasks, largeProjects, largeUsers);

        mockMvc.perform(get("/api/dashboard/analytics/summary")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.completed").value(9999))
                .andExpect(jsonPath("$.tasks.in_progress").value(8888))
                .andExpect(jsonPath("$.tasks.todo").value(7777))
                .andExpect(jsonPath("$.tasks.overdue").value(6666))
                .andExpect(jsonPath("$.projects.total").value(555))
                .andExpect(jsonPath("$.projects.completed").value(444))
                .andExpect(jsonPath("$.projects.in_progress").value(111))
                .andExpect(jsonPath("$.users.total").value(1000))
                .andExpect(jsonPath("$.users.online").value(500));

        verify(jdbcTemplate, times(3)).queryForMap(anyString());
    }

    @Test
    void testGetDashboardSummaryTimestampExists() throws Exception {
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(tasksMetrics, projectsMetrics, usersMetrics);

        mockMvc.perform(get("/api/dashboard/analytics/summary")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(jdbcTemplate, times(3)).queryForMap(anyString());
    }

    @Test
    void testGetDashboardSummaryAllFieldsPresent() throws Exception {
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(tasksMetrics, projectsMetrics, usersMetrics);

        mockMvc.perform(get("/api/dashboard/analytics/summary")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").exists())
                .andExpect(jsonPath("$.projects").exists())
                .andExpect(jsonPath("$.users").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tasks.completed").exists())
                .andExpect(jsonPath("$.tasks.in_progress").exists())
                .andExpect(jsonPath("$.tasks.todo").exists())
                .andExpect(jsonPath("$.tasks.overdue").exists())
                .andExpect(jsonPath("$.projects.total").exists())
                .andExpect(jsonPath("$.projects.completed").exists())
                .andExpect(jsonPath("$.projects.in_progress").exists())
                .andExpect(jsonPath("$.users.total").exists())
                .andExpect(jsonPath("$.users.online").exists());

        verify(jdbcTemplate, times(3)).queryForMap(anyString());
    }
}