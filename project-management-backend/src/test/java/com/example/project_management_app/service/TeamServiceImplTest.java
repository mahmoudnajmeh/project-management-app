package com.example.project_management_app.service;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private SecurityUtils security;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User me;
    private User admin;
    private User other;
    private Team team1;
    private Team team2;
    private List<Team> teams;

    @BeforeEach
    void setUp() {
        me = new User();
        me.setId(1L);
        me.setUsername("Mahmoud");
        me.setFirstName("Mahmoud");
        me.setLastName("Najmeh");
        me.setRole(User.Role.ROLE_USER);

        admin = new User();
        admin.setId(3L);
        admin.setUsername("Admin");
        admin.setRole(User.Role.ROLE_ADMIN);

        other = new User();
        other.setId(2L);
        other.setUsername("Katya");
        other.setFirstName("Katya");
        other.setLastName("Otto");
        other.setRole(User.Role.ROLE_USER);

        team1 = new Team();
        team1.setId(1L);
        team1.setName("Dev Team");
        team1.setDescription("Core dev team");
        team1.setCreatedBy(me);
        team1.setMembers(new ArrayList<>(Arrays.asList(me, other)));

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Marketing");
        team2.setDescription("Marketing team");
        team2.setCreatedBy(me);
        team2.setMembers(new ArrayList<>(Arrays.asList(me)));

        teams = Arrays.asList(team1, team2);
    }

    @Test
    void create_Works() {
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.save(any(Team.class))).thenReturn(team1);

        Team created = teamService.createTeam("Dev Team", "Core dev team");

        assertNotNull(created);
        assertEquals("Dev Team", created.getName());
        assertEquals(me, created.getCreatedBy());
        assertEquals(2, created.getMembers().size());
        assertTrue(created.getMembers().contains(me));

        verify(teamRepo, times(1)).save(any(Team.class));
    }

    @Test
    void getById_Works() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);

        Team found = teamService.getTeamById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Dev Team", found.getName());

        verify(teamRepo, times(1)).findById(1L);
    }

    @Test
    void getById_NotFound_Throws() {
        when(teamRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.getTeamById(99L));

        assertEquals("Team not found with id: 99", ex.getMessage());
    }

    @Test
    void getById_NoAccess_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(other);
        when(teamRepo.findByMemberId(other.getId())).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.getTeamById(1L));

        assertEquals("You don't have access to this team", ex.getMessage());
    }

    @Test
    void getAll_AsAdmin_ReturnsAll() {
        when(security.getCurrentUser()).thenReturn(admin);
        when(teamRepo.findAll()).thenReturn(teams);

        List<Team> result = teamService.getAllTeams();

        assertEquals(2, result.size());
        verify(teamRepo, times(1)).findAll();
        verify(teamRepo, never()).findByMemberId(anyLong());
    }

    @Test
    void getAll_AsUser_ReturnsMine() {
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);

        List<Team> result = teamService.getAllTeams();

        assertEquals(2, result.size());
        verify(teamRepo, never()).findAll();
        verify(teamRepo, times(1)).findByMemberId(me.getId());
    }

    @Test
    void getMine_Works() {
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);

        List<Team> result = teamService.getMyTeams();

        assertEquals(2, result.size());
        assertTrue(result.contains(team1));
        assertTrue(result.contains(team2));
        verify(teamRepo, times(1)).findByMemberId(me.getId());
    }

    @Test
    void getByUser_AsAdmin_Works() {
        when(security.getCurrentUser()).thenReturn(admin);
        when(userRepo.findById(1L)).thenReturn(Optional.of(me));
        when(teamRepo.findByMemberId(1L)).thenReturn(teams);

        List<Team> result = teamService.getTeamsByUser(1L);

        assertEquals(2, result.size());
        verify(teamRepo, times(1)).findByMemberId(1L);
    }

    @Test
    void getByUser_AsSelf_Works() {
        when(security.getCurrentUser()).thenReturn(me);
        when(userRepo.findById(1L)).thenReturn(Optional.of(me));
        when(teamRepo.findByMemberId(1L)).thenReturn(teams);

        List<Team> result = teamService.getTeamsByUser(1L);

        assertEquals(2, result.size());
        verify(teamRepo, times(1)).findByMemberId(1L);
    }

    @Test
    void getByUser_AsOther_Throws() {
        when(security.getCurrentUser()).thenReturn(me);
        when(userRepo.findById(2L)).thenReturn(Optional.of(other));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.getTeamsByUser(2L));

        assertEquals("You don't have permission to view this user's teams", ex.getMessage());
        verify(teamRepo, never()).findByMemberId(anyLong());
    }

    @Test
    void getByUser_UserNotFound_Throws() {
        when(security.getCurrentUser()).thenReturn(me);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.getTeamsByUser(99L));

        assertEquals("User not found with id: 99", ex.getMessage());
    }

    @Test
    void update_Works() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(teamRepo.save(any(Team.class))).thenReturn(team1);

        Team updated = teamService.updateTeam(1L, "New Name", "New Desc");

        assertEquals("New Name", updated.getName());
        assertEquals("New Desc", updated.getDescription());
        verify(teamRepo, times(1)).save(any(Team.class));
    }

    @Test
    void update_NotCreator_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(other);
        when(teamRepo.findByMemberId(other.getId())).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.updateTeam(1L, "New Name", "New Desc"));

        assertEquals("You don't have access to this team", ex.getMessage());
        verify(teamRepo, never()).save(any(Team.class));
    }

    @Test
    void delete_AsCreator_Works() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        doNothing().when(teamRepo).delete(team1);

        teamService.deleteTeam(1L);

        verify(teamRepo, times(1)).delete(team1);
    }

    @Test
    void delete_NotCreator_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(other);
        when(teamRepo.findByMemberId(other.getId())).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.deleteTeam(1L));

        assertEquals("You don't have access to this team", ex.getMessage());
        verify(teamRepo, never()).delete(any(Team.class));
    }

    @Test
    void addMember_Works() {
        User newUser = new User();
        newUser.setId(4L);
        newUser.setUsername("NewUser");

        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(userRepo.findById(4L)).thenReturn(Optional.of(newUser));
        when(teamRepo.save(any(Team.class))).thenReturn(team1);

        Team updated = teamService.addMemberToTeam(1L, 4L);

        assertNotNull(updated);
        verify(teamRepo, times(1)).save(any(Team.class));
    }

    @Test
    void addMember_NotCreator_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(other);
        when(teamRepo.findByMemberId(other.getId())).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.addMemberToTeam(1L, 3L));

        assertEquals("You don't have access to this team", ex.getMessage());
        verify(teamRepo, never()).save(any(Team.class));
    }

    @Test
    void addMember_UserNotFound_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.addMemberToTeam(1L, 99L));

        assertEquals("User not found with id: 99", ex.getMessage());
    }

    @Test
    void addMember_AlreadyMember_Succeeds() {
        // The service might not check for duplicates, so it succeeds
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(userRepo.findById(2L)).thenReturn(Optional.of(other));
        when(teamRepo.save(any(Team.class))).thenReturn(team1);

        Team updated = teamService.addMemberToTeam(1L, 2L);

        assertNotNull(updated);
        verify(teamRepo, times(1)).save(any(Team.class));
    }

    @Test
    void removeMember_Works() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(userRepo.findById(2L)).thenReturn(Optional.of(other));
        when(teamRepo.save(any(Team.class))).thenReturn(team1);

        Team updated = teamService.removeMemberFromTeam(1L, 2L);

        assertNotNull(updated);
        verify(teamRepo, times(1)).save(any(Team.class));
    }

    @Test
    void removeMember_NotCreator_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(other);
        when(teamRepo.findByMemberId(other.getId())).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.removeMemberFromTeam(1L, 2L));

        assertEquals("You don't have access to this team", ex.getMessage());
        verify(teamRepo, never()).save(any(Team.class));
    }

    @Test
    void removeMember_CannotRemoveCreator_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.removeMemberFromTeam(1L, 1L));

        assertEquals("Cannot remove the team creator", ex.getMessage());
        verify(teamRepo, never()).save(any(Team.class));
    }

    @Test
    void removeMember_UserNotFound_Throws() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.removeMemberFromTeam(1L, 99L));

        assertEquals("User not found with id: 99", ex.getMessage());
    }

    @Test
    void removeMember_NotMember_Throws() {
        User notMember = new User();
        notMember.setId(4L);

        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);
        when(userRepo.findById(4L)).thenReturn(Optional.of(notMember));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.removeMemberFromTeam(1L, 4L));

        assertEquals("User is not a member of this team", ex.getMessage());
        verify(teamRepo, never()).save(any(Team.class));
    }

    @Test
    void getMembers_Works() {
        Long teamId = 1L;

        List<User> members = Arrays.asList(me, other);

        when(teamRepo.findTeamMembers(teamId)).thenReturn(members);
        when(security.getCurrentUser()).thenReturn(me);
        when(teamRepo.findByMemberId(me.getId())).thenReturn(teams);

        List<User> result = teamService.getTeamMembers(teamId);

        assertEquals(2, result.size());
        verify(teamRepo, times(1)).findTeamMembers(teamId);
    }

    @Test
    void isUserInTeam_True() {
        when(teamRepo.findByMemberId(2L)).thenReturn(Arrays.asList(team1));

        boolean result = teamService.isUserInTeam(1L, 2L);

        assertTrue(result);
    }

    @Test
    void isUserInTeam_False() {
        when(teamRepo.findByMemberId(3L)).thenReturn(List.of());

        boolean result = teamService.isUserInTeam(1L, 3L);

        assertFalse(result);
    }

    @Test
    void isCreator_True() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));

        boolean result = teamService.isTeamCreator(1L, 1L);

        assertTrue(result);
    }

    @Test
    void isCreator_False() {
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team1));

        boolean result = teamService.isTeamCreator(1L, 2L);

        assertFalse(result);
    }

    @Test
    void isCreator_TeamNotFound_Throws() {
        when(teamRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> teamService.isTeamCreator(99L, 1L));

        assertEquals("Team not found with id: 99", ex.getMessage());
    }
}