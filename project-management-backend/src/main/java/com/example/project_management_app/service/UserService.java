package com.example.project_management_app.service;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AccountService accountService;

    public List<User> getUsersInSameTeam(User user) {
        List<Team> userTeams = teamRepository.findByMemberId(user.getId());

        if (userTeams.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> teamMembers = new ArrayList<>();
        for (Team team : userTeams) {
            List<User> members = teamRepository.findTeamMembers(team.getId());
            teamMembers.addAll(members);
        }

        return teamMembers.stream().distinct().toList();
    }

    public List<User> getUsersInProjectTeam(Long projectId) {
        User currentUser = accountService.getCurrentUser();
        List<Team> userTeams = teamRepository.findByMemberId(currentUser.getId());

        if (userTeams.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> teamMembers = new ArrayList<>();
        for (Team team : userTeams) {
            List<User> members = teamRepository.findTeamMembers(team.getId());
            teamMembers.addAll(members);
        }

        return teamMembers.stream().distinct().toList();
    }

    public List<User> getAllUsersInOrganization() {
        return userRepository.findAll();
    }
}