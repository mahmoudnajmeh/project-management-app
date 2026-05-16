package com.example.project_management_app.controller;

import com.example.project_management_app.pipeline.scheduler.MetricsScheduler;
import com.example.project_management_app.pipeline.service.RealTimeMetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RealTimeMetricsService realTimeMetricsService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MetricsScheduler metricsScheduler;

    @InjectMocks
    private AnalyticsController analyticsController;

    private ObjectMapper objectMapper;
    private Map<String, Object> realtimeMetrics;
    private List<Map<String, Object>> dailyMetricsList;
    private List<Map<String, Object>> teamPerformanceList;
    private Map<String, Object> userVelocityMap;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController).build();
        objectMapper = new ObjectMapper();

        realtimeMetrics = new HashMap<>();
        realtimeMetrics.put("tasksCreatedToday", 5);
        realtimeMetrics.put("tasksCompletedToday", 3);
        realtimeMetrics.put("activeUsersCount", 8);
        realtimeMetrics.put("averageUserVelocity", 2.5);
        realtimeMetrics.put("date", LocalDate.now().toString());

        Map<String, Object> dailyMetric1 = new HashMap<>();
        dailyMetric1.put("date", "2025-05-15");
        dailyMetric1.put("project_name", "MN ChatBot");
        dailyMetric1.put("team_name", "MN Team");
        dailyMetric1.put("total_tasks", 10);
        dailyMetric1.put("completed_tasks", 6);
        dailyMetric1.put("completion_rate", 60.0);

        Map<String, Object> dailyMetric2 = new HashMap<>();
        dailyMetric2.put("date", "2025-05-15");
        dailyMetric2.put("project_name", "Mobile App");
        dailyMetric2.put("team_name", "Mobile Team");
        dailyMetric2.put("total_tasks", 8);
        dailyMetric2.put("completed_tasks", 4);
        dailyMetric2.put("completion_rate", 50.0);

        dailyMetricsList = Arrays.asList(dailyMetric1, dailyMetric2);

        Map<String, Object> teamPerf1 = new HashMap<>();
        teamPerf1.put("team_name", "MN Team");
        teamPerf1.put("total_completed", 45);
        teamPerf1.put("total_tasks", 60);
        teamPerf1.put("avg_completion_rate", 75.0);
        teamPerf1.put("total_overdue", 5);
        teamPerf1.put("active_projects", 3);

        Map<String, Object> teamPerf2 = new HashMap<>();
        teamPerf2.put("team_name", "Mobile Team");
        teamPerf2.put("total_completed", 30);
        teamPerf2.put("total_tasks", 50);
        teamPerf2.put("avg_completion_rate", 60.0);
        teamPerf2.put("total_overdue", 8);
        teamPerf2.put("active_projects", 2);

        teamPerformanceList = Arrays.asList(teamPerf1, teamPerf2);

        userVelocityMap = new HashMap<>();
        userVelocityMap.put("tasks_completed", 12);
        userVelocityMap.put("first_task", "2025-05-01 10:00:00");
        userVelocityMap.put("last_task", "2025-05-15 16:30:00");
    }

    @Test
    void testGetRealtimeMetrics() throws Exception {
        when(realTimeMetricsService.getCurrentMetrics()).thenReturn(realtimeMetrics);

        mockMvc.perform(get("/api/analytics/realtime")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasksCreatedToday").value(5))
                .andExpect(jsonPath("$.tasksCompletedToday").value(3))
                .andExpect(jsonPath("$.activeUsersCount").value(8))
                .andExpect(jsonPath("$.averageUserVelocity").value(2.5));

        verify(realTimeMetricsService, times(1)).getCurrentMetrics();
    }

    @Test
    void testGetDailyMetricsSuccess() throws Exception {
        String date = "2025-05-15";
        LocalDate targetDate = LocalDate.parse(date);

        when(jdbcTemplate.queryForList(anyString(), eq(targetDate))).thenReturn(dailyMetricsList);

        mockMvc.perform(get("/api/analytics/daily/{date}", date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].project_name").value("MN ChatBot"))
                .andExpect(jsonPath("$[0].team_name").value("MN Team"))
                .andExpect(jsonPath("$[1].project_name").value("Mobile App"));

        verify(jdbcTemplate, times(1)).queryForList(anyString(), eq(targetDate));
    }

    @Test
    void testGetDailyMetricsEmptyResult() throws Exception {
        String date = "2025-05-16";
        LocalDate targetDate = LocalDate.parse(date);

        when(jdbcTemplate.queryForList(anyString(), eq(targetDate))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/analytics/daily/{date}", date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(jdbcTemplate, times(1)).queryForList(anyString(), eq(targetDate));
    }

    @Test
    void testGetTeamPerformance() throws Exception {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(teamPerformanceList);

        mockMvc.perform(get("/api/analytics/team-performance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].team_name").value("MN Team"))
                .andExpect(jsonPath("$[0].avg_completion_rate").value(75.0))
                .andExpect(jsonPath("$[1].team_name").value("Mobile Team"))
                .andExpect(jsonPath("$[1].avg_completion_rate").value(60.0));

        verify(jdbcTemplate, times(1)).queryForList(anyString());
    }

    @Test
    void testGetTeamPerformanceEmptyResult() throws Exception {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/analytics/team-performance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetUserVelocity() throws Exception {
        Long userId = 1L;

        when(jdbcTemplate.queryForMap(anyString(), eq(userId))).thenReturn(userVelocityMap);

        mockMvc.perform(get("/api/analytics/user-velocity/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks_completed").value(12))
                .andExpect(jsonPath("$.first_task").value("2025-05-01 10:00:00"))
                .andExpect(jsonPath("$.last_task").value("2025-05-15 16:30:00"));

        verify(jdbcTemplate, times(1)).queryForMap(anyString(), eq(userId));
    }

    @Test
    void testGetUserVelocityNoTasks() throws Exception {
        Long userId = 999L;
        Map<String, Object> emptyResult = new HashMap<>();
        emptyResult.put("tasks_completed", 0);
        emptyResult.put("first_task", null);
        emptyResult.put("last_task", null);

        when(jdbcTemplate.queryForMap(anyString(), eq(userId))).thenReturn(emptyResult);

        mockMvc.perform(get("/api/analytics/user-velocity/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks_completed").value(0));

        verify(jdbcTemplate, times(1)).queryForMap(anyString(), eq(userId));
    }

    @Test
    void testTriggerDailyMetricsSuccess() throws Exception {
        doNothing().when(metricsScheduler).runDailyMetricsJob();

        mockMvc.perform(post("/api/analytics/trigger-daily-metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Daily metrics job triggered successfully"));

        verify(metricsScheduler, times(1)).runDailyMetricsJob();
    }

    @Test
    void testTriggerDailyMetricsError() throws Exception {
        doThrow(new RuntimeException("Database connection failed")).when(metricsScheduler).runDailyMetricsJob();

        mockMvc.perform(post("/api/analytics/trigger-daily-metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Database connection failed"));

        verify(metricsScheduler, times(1)).runDailyMetricsJob();
    }

    @Test
    void testGetRealtimeMetricsEmptyData() throws Exception {
        Map<String, Object> emptyMetrics = new HashMap<>();
        emptyMetrics.put("tasksCreatedToday", 0);
        emptyMetrics.put("tasksCompletedToday", 0);
        emptyMetrics.put("activeUsersCount", 0);
        emptyMetrics.put("averageUserVelocity", 0.0);
        emptyMetrics.put("date", LocalDate.now().toString());

        when(realTimeMetricsService.getCurrentMetrics()).thenReturn(emptyMetrics);

        mockMvc.perform(get("/api/analytics/realtime")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasksCreatedToday").value(0))
                .andExpect(jsonPath("$.tasksCompletedToday").value(0))
                .andExpect(jsonPath("$.activeUsersCount").value(0))
                .andExpect(jsonPath("$.averageUserVelocity").value(0.0));
    }

    @Test
    void testGetUserVelocityWithLargeUserId() throws Exception {
        Long userId = 999999L;
        Map<String, Object> result = new HashMap<>();
        result.put("tasks_completed", 0);
        result.put("first_task", null);
        result.put("last_task", null);

        when(jdbcTemplate.queryForMap(anyString(), eq(userId))).thenReturn(result);

        mockMvc.perform(get("/api/analytics/user-velocity/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(jdbcTemplate, times(1)).queryForMap(anyString(), eq(userId));
    }
}