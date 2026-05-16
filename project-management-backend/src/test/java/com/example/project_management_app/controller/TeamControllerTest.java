package com.example.project_management_app.controller;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.TeamService;
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
class TeamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private ObjectMapper objectMapper;
    private Team team1;
    private Team team2;
    private User user1;
    private User user2;
    private List<Team> teamList;
    private List<User> memberList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamController).build();
        objectMapper = new ObjectMapper();

        user1 = new User();
        user1.setId(1L);
        user1.setUsername("Mahmoud");
        user1.setFirstName("Mahmoud");
        user1.setLastName("Najmeh");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("Katya");
        user2.setFirstName("Katya");
        user2.setLastName("Otto");

        team1 = new Team();
        team1.setId(1L);
        team1.setName("Development Team");
        team1.setDescription("Core development team");
        team1.setCreatedBy(user1);

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Marketing Team");
        team2.setDescription("Marketing and sales");
        team2.setCreatedBy(user1);

        teamList = Arrays.asList(team1, team2);
        memberList = Arrays.asList(user1, user2);
    }

    @Test
    void getAllTeams_Works() throws Exception {
        when(teamService.getAllTeams()).thenReturn(teamList);

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Development Team"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Marketing Team"));

        verify(teamService, times(1)).getAllTeams();
    }

    @Test
    void getMyTeams_Works() throws Exception {
        when(teamService.getMyTeams()).thenReturn(teamList);

        mockMvc.perform(get("/api/teams/my-teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(teamService, times(1)).getMyTeams();
    }

    @Test
    void getTeamsByUser_Works() throws Exception {
        Long userId = 1L;
        when(teamService.getTeamsByUser(userId)).thenReturn(teamList);

        mockMvc.perform(get("/api/teams/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(teamService, times(1)).getTeamsByUser(userId);
    }

    @Test
    void getTeam_Works() throws Exception {
        Long teamId = 1L;
        when(teamService.getTeamById(teamId)).thenReturn(team1);

        mockMvc.perform(get("/api/teams/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Development Team"))
                .andExpect(jsonPath("$.description").value("Core development team"));

        verify(teamService, times(1)).getTeamById(teamId);
    }

    @Test
    void createTeam_Works() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "New Team");
        request.put("description", "New team description");

        when(teamService.createTeam("New Team", "New team description")).thenReturn(team1);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Development Team"));

        verify(teamService, times(1)).createTeam("New Team", "New team description");
    }

    @Test
    void createTeam_MissingName_ReturnsBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("description", "New team description");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(teamService, never()).createTeam(anyString(), anyString());
    }

    @Test
    void createTeam_EmptyName_ReturnsBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "");
        request.put("description", "New team description");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(teamService, never()).createTeam(anyString(), anyString());
    }

    @Test
    void createTeam_BlankName_ReturnsBadRequest() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "   ");
        request.put("description", "New team description");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(teamService, never()).createTeam(anyString(), anyString());
    }

    @Test
    void updateTeam_Works() throws Exception {
        Long teamId = 1L;
        Map<String, String> request = new HashMap<>();
        request.put("name", "Updated Team");
        request.put("description", "Updated description");

        when(teamService.updateTeam(eq(teamId), eq("Updated Team"), eq("Updated description")))
                .thenReturn(team1);

        mockMvc.perform(put("/api/teams/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(teamService, times(1)).updateTeam(eq(teamId), eq("Updated Team"), eq("Updated description"));
    }

    @Test
    void updateTeam_Forbidden_Returns403() throws Exception {
        Long teamId = 1L;
        Map<String, String> request = new HashMap<>();
        request.put("name", "Updated Team");

        when(teamService.updateTeam(eq(teamId), eq("Updated Team"), isNull()))
                .thenThrow(new RuntimeException("Not authorized to update this team"));

        mockMvc.perform(put("/api/teams/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to update this team"));

        verify(teamService, times(1)).updateTeam(eq(teamId), eq("Updated Team"), isNull());
    }

    @Test
    void deleteTeam_Works() throws Exception {
        Long teamId = 1L;
        doNothing().when(teamService).deleteTeam(teamId);

        mockMvc.perform(delete("/api/teams/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Team deleted successfully"));

        verify(teamService, times(1)).deleteTeam(teamId);
    }

    @Test
    void deleteTeam_Forbidden_Returns403() throws Exception {
        Long teamId = 1L;
        doThrow(new RuntimeException("Not authorized to delete this team"))
                .when(teamService).deleteTeam(teamId);

        mockMvc.perform(delete("/api/teams/{id}", teamId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to delete this team"));

        verify(teamService, times(1)).deleteTeam(teamId);
    }

    @Test
    void getTeamMembers_Works() throws Exception {
        Long teamId = 1L;
        when(teamService.getTeamMembers(teamId)).thenReturn(memberList);

        mockMvc.perform(get("/api/teams/{teamId}/members", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("Mahmoud"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("Katya"));

        verify(teamService, times(1)).getTeamMembers(teamId);
    }

    @Test
    void getTeamMembers_Forbidden_Returns403() throws Exception {
        Long teamId = 1L;
        when(teamService.getTeamMembers(teamId))
                .thenThrow(new RuntimeException("Not authorized to view members"));

        mockMvc.perform(get("/api/teams/{teamId}/members", teamId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to view members"));

        verify(teamService, times(1)).getTeamMembers(teamId);
    }

    @Test
    void addMemberToTeam_Works() throws Exception {
        Long teamId = 1L;
        Long userId = 2L;
        when(teamService.addMemberToTeam(teamId, userId)).thenReturn(team1);

        mockMvc.perform(post("/api/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(teamService, times(1)).addMemberToTeam(teamId, userId);
    }

    @Test
    void addMemberToTeam_Forbidden_Returns403() throws Exception {
        Long teamId = 1L;
        Long userId = 2L;
        when(teamService.addMemberToTeam(teamId, userId))
                .thenThrow(new RuntimeException("Not authorized to add members"));

        mockMvc.perform(post("/api/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to add members"));

        verify(teamService, times(1)).addMemberToTeam(teamId, userId);
    }

    @Test
    void removeMemberFromTeam_Works() throws Exception {
        Long teamId = 1L;
        Long userId = 2L;
        when(teamService.removeMemberFromTeam(teamId, userId)).thenReturn(team1);

        mockMvc.perform(delete("/api/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member removed successfully"));

        verify(teamService, times(1)).removeMemberFromTeam(teamId, userId);
    }

    @Test
    void removeMemberFromTeam_Forbidden_Returns403() throws Exception {
        Long teamId = 1L;
        Long userId = 2L;
        when(teamService.removeMemberFromTeam(teamId, userId))
                .thenThrow(new RuntimeException("Not authorized to remove members"));

        mockMvc.perform(delete("/api/teams/{teamId}/members/{userId}", teamId, userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to remove members"));

        verify(teamService, times(1)).removeMemberFromTeam(teamId, userId);
    }

    @Test
    void checkUserInTeam_ReturnsTrue() throws Exception {
        Long teamId = 1L;
        Long userId = 1L;
        when(teamService.isUserInTeam(teamId, userId)).thenReturn(true);

        mockMvc.perform(get("/api/teams/{teamId}/members/{userId}/check", teamId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isInTeam").value(true));

        verify(teamService, times(1)).isUserInTeam(teamId, userId);
    }

    @Test
    void checkUserInTeam_ReturnsFalse() throws Exception {
        Long teamId = 1L;
        Long userId = 3L;
        when(teamService.isUserInTeam(teamId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/teams/{teamId}/members/{userId}/check", teamId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isInTeam").value(false));

        verify(teamService, times(1)).isUserInTeam(teamId, userId);
    }
}