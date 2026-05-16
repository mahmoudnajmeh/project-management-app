package com.example.project_management_app.controller;

import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.service.AccountService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private SearchController searchController;

    private User currentUser;
    private User otherUser;
    private Project project1;
    private Project project2;
    private Task task1;
    private Task task2;
    private Team team1;
    private Team team2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("Mahmoud");
        currentUser.setFirstName("Mahmoud");
        currentUser.setLastName("Najmeh");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("Katya");
        otherUser.setFirstName("Katya");
        otherUser.setLastName("Otto");

        project1 = new Project();
        project1.setId(1L);
        project1.setName("MN ChatBot");
        project1.setDescription("AI Chatbot project");

        project2 = new Project();
        project2.setId(2L);
        project2.setName("Mobile App");
        project2.setDescription("Mobile application");

        task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Implement login");
        task1.setDescription("Add OAuth2 authentication");

        task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Fix bug");
        task2.setDescription("Fix authentication bug");

        team1 = new Team();
        team1.setId(1L);
        team1.setName("MN Team");
        team1.setDescription("Main development team");

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Mobile Team");
        team2.setDescription("Mobile development team");
    }

    @Test
    void testSearchWithEmptyQuery() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/api/search")
                        .param("q", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, never()).findByNameContainingIgnoreCase(anyString());
        verify(taskRepository, never()).findByTitleContainingIgnoreCase(anyString());
        verify(teamRepository, never()).findByNameContainingIgnoreCase(anyString());
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(anyString());
    }

    @Test
    void testSearchWithShortQuery() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/api/search")
                        .param("q", "a")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountService, times(1)).getCurrentUser();
        verify(projectRepository, never()).findByNameContainingIgnoreCase(anyString());
        verify(taskRepository, never()).findByTitleContainingIgnoreCase(anyString());
        verify(teamRepository, never()).findByNameContainingIgnoreCase(anyString());
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(anyString());
    }

    @Test
    void testSearchProjectsOnly() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("chat")).thenReturn(Arrays.asList(project1));
        when(taskRepository.findByTitleContainingIgnoreCase("chat")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("chat")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("chat")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "chat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("MN ChatBot"))
                .andExpect(jsonPath("$[0].type").value("project"))
                .andExpect(jsonPath("$[0].url").value("/projects/1"));
    }

    @Test
    void testSearchTasksOnly() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("login")).thenReturn(Collections.emptyList());
        when(taskRepository.findByTitleContainingIgnoreCase("login")).thenReturn(Arrays.asList(task1));
        when(teamRepository.findByNameContainingIgnoreCase("login")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("login")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Implement login"))
                .andExpect(jsonPath("$[0].type").value("task"))
                .andExpect(jsonPath("$[0].url").value("/tasks/1"));
    }

    @Test
    void testSearchTeamsOnly() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("team")).thenReturn(Collections.emptyList());
        when(taskRepository.findByTitleContainingIgnoreCase("team")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("team")).thenReturn(Arrays.asList(team1));
        when(userRepository.findByUsernameContainingIgnoreCase("team")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("MN Team"))
                .andExpect(jsonPath("$[0].type").value("team"))
                .andExpect(jsonPath("$[0].url").value("/team/1"));
    }

    @Test
    void testSearchUsersOnly() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("katya")).thenReturn(Collections.emptyList());
        when(taskRepository.findByTitleContainingIgnoreCase("katya")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("katya")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("katya")).thenReturn(Arrays.asList(otherUser));

        mockMvc.perform(get("/api/search")
                        .param("q", "katya")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Katya Otto"))
                .andExpect(jsonPath("$[0].description").value("@Katya"))
                .andExpect(jsonPath("$[0].type").value("user"))
                .andExpect(jsonPath("$[0].url").value("/profile/2"));
    }

    @Test
    void testSearchExcludesCurrentUser() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("mahmoud")).thenReturn(Collections.emptyList());
        when(taskRepository.findByTitleContainingIgnoreCase("mahmoud")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("mahmoud")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("mahmoud")).thenReturn(Arrays.asList(currentUser, otherUser));

        mockMvc.perform(get("/api/search")
                        .param("q", "mahmoud")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Katya Otto"));
    }

    @Test
    void testSearchAllTypes() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("app")).thenReturn(Arrays.asList(project1, project2));
        when(taskRepository.findByTitleContainingIgnoreCase("app")).thenReturn(Arrays.asList(task1, task2));
        when(teamRepository.findByNameContainingIgnoreCase("app")).thenReturn(Arrays.asList(team1, team2));
        when(userRepository.findByUsernameContainingIgnoreCase("app")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "app")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void testSearchNoResults() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());
        when(taskRepository.findByTitleContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("chatbot")).thenReturn(Arrays.asList(project1));
        when(taskRepository.findByTitleContainingIgnoreCase("chatbot")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("chatbot")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("chatbot")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "chatbot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("MN ChatBot"));
    }

    @Test
    void testSearchCaseInsensitive() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByNameContainingIgnoreCase("CHAT")).thenReturn(Arrays.asList(project1));
        when(taskRepository.findByTitleContainingIgnoreCase("CHAT")).thenReturn(Collections.emptyList());
        when(teamRepository.findByNameContainingIgnoreCase("CHAT")).thenReturn(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("CHAT")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search")
                        .param("q", "CHAT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("MN ChatBot"));
    }
}