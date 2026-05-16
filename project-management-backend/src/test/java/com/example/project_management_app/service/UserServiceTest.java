package com.example.project_management_app.service;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private User user1;
    private User user2;
    private User user3;
    private Team team1;
    private Team team2;
    private List<Team> userTeams;
    private List<User> team1Members;
    private List<User> team2Members;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("Mahmoud");
        currentUser.setFirstName("Mahmoud");
        currentUser.setLastName("Najmeh");
        currentUser.setEmail("mn.de@outlook.com");
        currentUser.setRole(User.Role.ROLE_USER);

        user1 = new User();
        user1.setId(1L);
        user1.setUsername("Mahmoud");
        user1.setFirstName("Mahmoud");
        user1.setLastName("Najmeh");
        user1.setEmail("mn.de@outlook.com");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("Katya");
        user2.setFirstName("Katya");
        user2.setLastName("Otto");
        user2.setEmail("mamocool3@gmail.com");

        user3 = new User();
        user3.setId(3L);
        user3.setUsername("John");
        user3.setFirstName("John");
        user3.setLastName("Doe");
        user3.setEmail("john.doe@example.com");

        team1 = new Team();
        team1.setId(1L);
        team1.setName("Development Team");

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Marketing Team");

        userTeams = Arrays.asList(team1, team2);
        team1Members = Arrays.asList(user1, user2);
        team2Members = Arrays.asList(user1, user3);
    }

    @Test
    void getUsersInSameTeamSuccess() {
        when(teamRepository.findByMemberId(1L)).thenReturn(userTeams);
        when(teamRepository.findTeamMembers(1L)).thenReturn(team1Members);
        when(teamRepository.findTeamMembers(2L)).thenReturn(team2Members);

        List<User> result = userService.getUsersInSameTeam(user1);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(2L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(3L)));

        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, times(1)).findTeamMembers(2L);
    }

    @Test
    void getUsersInSameTeamWithDuplicateMembers() {
        List<User> team1MembersWithDuplicates = Arrays.asList(user1, user2, user1);
        List<User> team2MembersWithDuplicates = Arrays.asList(user1, user3, user2);

        when(teamRepository.findByMemberId(1L)).thenReturn(userTeams);
        when(teamRepository.findTeamMembers(1L)).thenReturn(team1MembersWithDuplicates);
        when(teamRepository.findTeamMembers(2L)).thenReturn(team2MembersWithDuplicates);

        List<User> result = userService.getUsersInSameTeam(user1);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(2L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(3L)));

        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, times(1)).findTeamMembers(2L);
    }

    @Test
    void getUsersInSameTeamSingleTeam() {
        List<Team> singleTeam = Arrays.asList(team1);

        when(teamRepository.findByMemberId(1L)).thenReturn(singleTeam);
        when(teamRepository.findTeamMembers(1L)).thenReturn(team1Members);

        List<User> result = userService.getUsersInSameTeam(user1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(2L)));

        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, never()).findTeamMembers(2L);
    }

    @Test
    void getUsersInSameTeamNoTeams() {
        when(teamRepository.findByMemberId(1L)).thenReturn(Collections.emptyList());

        List<User> result = userService.getUsersInSameTeam(user1);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, never()).findTeamMembers(anyLong());
    }

    @Test
    void getUsersInSameTeamEmptyTeamMembers() {
        when(teamRepository.findByMemberId(1L)).thenReturn(userTeams);
        when(teamRepository.findTeamMembers(1L)).thenReturn(Collections.emptyList());
        when(teamRepository.findTeamMembers(2L)).thenReturn(Collections.emptyList());

        List<User> result = userService.getUsersInSameTeam(user1);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, times(1)).findTeamMembers(2L);
    }

    @Test
    void getUsersInProjectTeamSuccess() {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(teamRepository.findByMemberId(1L)).thenReturn(userTeams);
        when(teamRepository.findTeamMembers(1L)).thenReturn(team1Members);
        when(teamRepository.findTeamMembers(2L)).thenReturn(team2Members);

        List<User> result = userService.getUsersInProjectTeam(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(2L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(3L)));

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, times(1)).findTeamMembers(2L);
    }

    @Test
    void getUsersInProjectTeamWithDuplicateMembers() {
        List<User> team1MembersWithDuplicates = Arrays.asList(user1, user2, user1);
        List<User> team2MembersWithDuplicates = Arrays.asList(user1, user3, user2);

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(teamRepository.findByMemberId(1L)).thenReturn(userTeams);
        when(teamRepository.findTeamMembers(1L)).thenReturn(team1MembersWithDuplicates);
        when(teamRepository.findTeamMembers(2L)).thenReturn(team2MembersWithDuplicates);

        List<User> result = userService.getUsersInProjectTeam(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(2L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(3L)));

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, times(1)).findTeamMembers(2L);
    }

    @Test
    void getUsersInProjectTeamSingleTeam() {
        List<Team> singleTeam = Arrays.asList(team1);

        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(teamRepository.findByMemberId(1L)).thenReturn(singleTeam);
        when(teamRepository.findTeamMembers(1L)).thenReturn(team1Members);

        List<User> result = userService.getUsersInProjectTeam(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(2L)));

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, never()).findTeamMembers(2L);
    }

    @Test
    void getUsersInProjectTeamNoTeams() {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(teamRepository.findByMemberId(1L)).thenReturn(Collections.emptyList());

        List<User> result = userService.getUsersInProjectTeam(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, never()).findTeamMembers(anyLong());
    }

    @Test
    void getUsersInProjectTeamEmptyTeamMembers() {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        when(teamRepository.findByMemberId(1L)).thenReturn(userTeams);
        when(teamRepository.findTeamMembers(1L)).thenReturn(Collections.emptyList());
        when(teamRepository.findTeamMembers(2L)).thenReturn(Collections.emptyList());

        List<User> result = userService.getUsersInProjectTeam(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accountService, times(1)).getCurrentUser();
        verify(teamRepository, times(1)).findByMemberId(1L);
        verify(teamRepository, times(1)).findTeamMembers(1L);
        verify(teamRepository, times(1)).findTeamMembers(2L);
    }

    @Test
    void getAllUsersInOrganizationSuccess() {
        List<User> allUsers = Arrays.asList(user1, user2, user3);
        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> result = userService.getAllUsersInOrganization();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsersInOrganizationEmpty() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsersInOrganization();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll();
    }
}