package com.example.project_management_app.service;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TeamService {
    Team createTeam(String name, String description);
    Team getTeamById(Long id);
    List<Team> getAllTeams();
    List<Team> getMyTeams();
    List<Team> getTeamsByUser(Long userId);
    Team updateTeam(Long id, String name, String description);
    void deleteTeam(Long id);
    Team addMemberToTeam(Long teamId, Long userId);
    Team removeMemberFromTeam(Long teamId, Long userId);
    List<User> getTeamMembers(Long teamId);
    boolean isUserInTeam(Long teamId, Long userId);
    boolean isTeamCreator(Long teamId, Long userId);
    Team updateTeamPhoto(Long teamId, MultipartFile file) throws Exception;
    void deleteTeamPhoto(Long teamId) throws Exception;
}