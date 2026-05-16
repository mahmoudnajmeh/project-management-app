package com.example.project_management_app.controller;

import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AIAssistantService;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.UserService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AIAssistantService aiAssistantService;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AIController aiController;

    private ObjectMapper objectMapper;
    private User testUser;
    private List<User> teamMembers;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Mahmoud");
        testUser.setEmail("mn.de@outlook.com");
        testUser.setFirstName("Mahmoud");
        testUser.setLastName("Najmeh");

        User member1 = new User();
        member1.setId(2L);
        member1.setUsername("Katya");
        member1.setFirstName("Katya");
        member1.setLastName("Otto");

        User member2 = new User();
        member2.setId(3L);
        member2.setUsername("John");
        member2.setFirstName("John");
        member2.setLastName("Doe");

        teamMembers = Arrays.asList(member1, member2);
    }

    @Test
    void testGetProjectSummarySuccess() throws Exception {
        String expectedSummary = "Project summary: Completed 5 tasks, 2 in progress";
        when(aiAssistantService.generateProjectSummary(1L)).thenReturn(expectedSummary);

        mockMvc.perform(post("/api/ai/project-summary/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedSummary));

        verify(aiAssistantService, times(1)).generateProjectSummary(1L);
    }

    @Test
    void testGetProjectSummaryServiceError() throws Exception {
        when(aiAssistantService.generateProjectSummary(1L))
                .thenThrow(new RuntimeException("AI service connection failed"));

        mockMvc.perform(post("/api/ai/project-summary/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("AI service unavailable: AI service connection failed"));

        verify(aiAssistantService, times(1)).generateProjectSummary(1L);
    }

    @Test
    void testGetTaskPrioritySuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("title", "Implement login feature");
        request.put("description", "Add OAuth2 authentication with Google and GitHub");

        String expectedRecommendation = "HIGH";
        when(aiAssistantService.recommendTaskPriority(anyString(), anyString())).thenReturn(expectedRecommendation);

        mockMvc.perform(post("/api/ai/task-priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendation").value(expectedRecommendation));

        verify(aiAssistantService, times(1)).recommendTaskPriority(eq("Implement login feature"), eq("Add OAuth2 authentication with Google and GitHub"));
    }

    @Test
    void testGetTaskPriorityMissingTitle() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("description", "Missing title field");

        when(aiAssistantService.recommendTaskPriority(eq(null), anyString()))
                .thenThrow(new RuntimeException("Title is required"));

        mockMvc.perform(post("/api/ai/task-priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("AI service unavailable: Title is required"));
    }

    @Test
    void testAskQuestionSuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("question", "What tasks are overdue?");

        String expectedAnswer = "You have 3 overdue tasks: Fix login bug, Update documentation, Deploy to production";
        when(accountService.getCurrentUser()).thenReturn(testUser);
        when(aiAssistantService.answerUserQuestion(eq(1L), eq("What tasks are overdue?"))).thenReturn(expectedAnswer);

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(expectedAnswer));

        verify(accountService, times(1)).getCurrentUser();
        verify(aiAssistantService, times(1)).answerUserQuestion(1L, "What tasks are overdue?");
    }

    @Test
    void testAskQuestionEmptyQuestion() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("question", "");

        when(accountService.getCurrentUser()).thenReturn(testUser);
        when(aiAssistantService.answerUserQuestion(eq(1L), eq("")))
                .thenThrow(new RuntimeException("Question cannot be empty"));

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("AI service unavailable: Question cannot be empty"));
    }

    @Test
    void testSuggestMembersSuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("description", "Need someone with React and Spring Boot experience for a full-stack task");

        String expectedSuggestions = "Recommended: Katya Otto (React expert), John Doe (Spring Boot specialist)";
        when(accountService.getCurrentUser()).thenReturn(testUser);
        when(userService.getUsersInSameTeam(testUser)).thenReturn(teamMembers);
        when(aiAssistantService.suggestTeamMembers(eq("Need someone with React and Spring Boot experience for a full-stack task"), eq(teamMembers)))
                .thenReturn(expectedSuggestions);

        mockMvc.perform(post("/api/ai/suggest-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").value(expectedSuggestions));

        verify(accountService, times(1)).getCurrentUser();
        verify(userService, times(1)).getUsersInSameTeam(testUser);
        verify(aiAssistantService, times(1)).suggestTeamMembers(anyString(), eq(teamMembers));
    }

    @Test
    void testSuggestMembersEmptyTeam() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("description", "Need team member for task");

        when(accountService.getCurrentUser()).thenReturn(testUser);
        when(userService.getUsersInSameTeam(testUser)).thenReturn(Arrays.asList());
        when(aiAssistantService.suggestTeamMembers(anyString(), eq(Arrays.asList())))
                .thenThrow(new RuntimeException("No team members found"));

        mockMvc.perform(post("/api/ai/suggest-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("AI service unavailable: No team members found"));
    }

    @Test
    void testGetProjectSummaryWithInvalidProjectId() throws Exception {
        when(aiAssistantService.generateProjectSummary(999L))
                .thenThrow(new RuntimeException("Project not found with id: 999"));

        mockMvc.perform(post("/api/ai/project-summary/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("AI service unavailable: Project not found with id: 999"));
    }

    @Test
    void testAskQuestionWithSpecialCharacters() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("question", "What's the status of task #123? & show @mentions");

        String expectedAnswer = "Task #123 is in progress. @mentions: John, Katya";
        when(accountService.getCurrentUser()).thenReturn(testUser);
        when(aiAssistantService.answerUserQuestion(eq(1L), eq("What's the status of task #123? & show @mentions")))
                .thenReturn(expectedAnswer);

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(expectedAnswer));
    }

    @Test
    void testAllEndpointsWithAuthenticationError() throws Exception {
        when(accountService.getCurrentUser()).thenThrow(new RuntimeException("User not authenticated"));

        Map<String, String> request = new HashMap<>();
        request.put("question", "Any tasks?");

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("AI service unavailable: User not authenticated"));
    }
}