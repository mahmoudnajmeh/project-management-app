package com.example.project_management_app.service;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.util.SecurityUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public Team createTeam(String name, String description) {
        User currentUser = securityUtils.getCurrentUser();

        Team team = new Team(name, description, currentUser);

        team.setMembers(List.of(currentUser));

        return teamRepository.save(team);
    }

    @Override
    public Team getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));

        User currentUser = securityUtils.getCurrentUser();
        if (!isUserInTeam(id, currentUser.getId()) && !isTeamCreator(id, currentUser.getId()) &&
                !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have access to this team");
        }

        // Force loading of user data including profile pictures
        if (team.getCreatedBy() != null) {
            team.getCreatedBy().getProfilePictureFileName();
        }
        if (team.getMembers() != null) {
            team.getMembers().forEach(member -> {
                member.getProfilePictureFileName();
            });
        }

        // Set team photo URL using FileStorageService
        if (team.getTeamPhotoFileName() != null) {
            team.setTeamPhotoUrl(fileStorageService.getTeamPhotoUrl(team.getId()));
        }

        return team;
    }

    @Override
    public List<Team> getAllTeams() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getRole().name().equals("ROLE_ADMIN")) {
            List<Team> teams = teamRepository.findAll();
            // Force loading of user data
            teams.forEach(team -> {
                if (team.getCreatedBy() != null) {
                    team.getCreatedBy().getProfilePictureFileName();
                }
                if (team.getMembers() != null) {
                    team.getMembers().forEach(member -> {
                        member.getProfilePictureFileName();
                    });
                }
                // Set team photo URL using FileStorageService
                if (team.getTeamPhotoFileName() != null) {
                    team.setTeamPhotoUrl(fileStorageService.getTeamPhotoUrl(team.getId()));
                }
            });
            return teams;
        }
        return getMyTeams();
    }

    @Override
    public List<Team> getMyTeams() {
        User currentUser = securityUtils.getCurrentUser();
        List<Team> teams = teamRepository.findByMemberId(currentUser.getId());

        for (Team team : teams) {
            Hibernate.initialize(team.getMembers());
            Hibernate.initialize(team.getCreatedBy());

            if (team.getTeamPhotoFileName() != null) {
                team.setTeamPhotoUrl(fileStorageService.getTeamPhotoUrl(team.getId()));
            }
        }

        return teams;
    }

    @Override
    public List<Team> getTeamsByUser(Long userId) {
        User currentUser = securityUtils.getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!currentUser.getId().equals(userId) && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have permission to view this user's teams");
        }

        List<Team> teams = teamRepository.findByMemberId(userId);

        teams.forEach(team -> {
            if (team.getCreatedBy() != null) {
                team.getCreatedBy().getProfilePictureFileName();
            }
            if (team.getMembers() != null) {
                team.getMembers().forEach(member -> {
                    member.getProfilePictureFileName();
                });
            }
            // Set team photo URL using FileStorageService
            if (team.getTeamPhotoFileName() != null) {
                team.setTeamPhotoUrl(fileStorageService.getTeamPhotoUrl(team.getId()));
            }
        });

        return teams;
    }

    @Override
    public Team updateTeam(Long id, String name, String description) {
        Team team = getTeamById(id);

        User currentUser = securityUtils.getCurrentUser();
        if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the team creator can update the team");
        }

        team.setName(name);
        team.setDescription(description);

        return teamRepository.save(team);
    }

    @Override
    public void deleteTeam(Long id) {
        Team team = getTeamById(id);

        User currentUser = securityUtils.getCurrentUser();
        if (!team.getCreatedBy().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only the team creator or admin can delete the team");
        }

        // Delete team photo if exists
        if (team.getTeamPhotoFileName() != null) {
            try {
                fileStorageService.deleteTeamPhoto(team.getTeamPhotoFileName());
            } catch (IOException e) {
                System.err.println("Failed to delete team photo: " + e.getMessage());
            }
        }

        teamRepository.delete(team);
    }

    @Override
    public Team addMemberToTeam(Long teamId, Long userId) {
        Team team = getTeamById(teamId);
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        User currentUser = securityUtils.getCurrentUser();
        if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the team creator can add members");
        }

        if (isUserInTeam(teamId, userId)) {
            throw new RuntimeException("User is already a member of this team");
        }

        team.getMembers().add(userToAdd);
        return teamRepository.save(team);
    }

    @Override
    public Team removeMemberFromTeam(Long teamId, Long userId) {
        Team team = getTeamById(teamId);

        User currentUser = securityUtils.getCurrentUser();
        if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the team creator can remove members");
        }

        if (team.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Cannot remove the team creator");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        boolean removed = team.getMembers().removeIf(member -> member.getId().equals(userId));
        if (!removed) {
            throw new RuntimeException("User is not a member of this team");
        }

        return teamRepository.save(team);
    }

    @Override
    public List<User> getTeamMembers(Long teamId) {
        // First verify user has access to this team
        User currentUser = securityUtils.getCurrentUser();
        if (!isUserInTeam(teamId, currentUser.getId()) && !isTeamCreator(teamId, currentUser.getId()) &&
                !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have access to this team");
        }

        // Use the dedicated query that only returns users, not teams
        List<User> members = teamRepository.findTeamMembers(teamId);

        // Initialize profile picture fields
        members.forEach(member -> {
            member.getProfilePictureFileName();
            member.getProfilePictureUrl();
            member.getProfilePicturePath();
        });

        return members;
    }

    @Override
    public boolean isUserInTeam(Long teamId, Long userId) {
        return teamRepository.findByMemberId(userId).stream()
                .anyMatch(team -> team.getId().equals(teamId));
    }

    @Override
    public boolean isTeamCreator(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        return team.getCreatedBy().getId().equals(userId);
    }

    @Override
    public Team updateTeamPhoto(Long teamId, MultipartFile file) throws Exception {
        Team team = getTeamById(teamId);

        User currentUser = securityUtils.getCurrentUser();
        if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the team creator can update the team photo");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        if (team.getTeamPhotoFileName() != null) {
            try {
                fileStorageService.deleteTeamPhoto(team.getTeamPhotoFileName());
            } catch (IOException e) {
                System.err.println("Failed to delete old team photo: " + e.getMessage());
            }
        }

        // Debug: Log file info before storing
        System.out.println("Original filename: " + file.getOriginalFilename());
        System.out.println("Content type: " + file.getContentType());
        System.out.println("File size: " + file.getSize());

        String fileName = fileStorageService.storeTeamPhoto(file, teamId);

        // Debug: Log the generated filename
        System.out.println("Generated filename: " + fileName);

        team.setTeamPhotoFileName(fileName);
        team.setTeamPhotoContentType(contentType);
        team.setTeamPhotoSize(file.getSize());
        team.setTeamPhotoPath("./uploads/team-photos/" + fileName);
        team.setTeamPhotoUrl(fileStorageService.getTeamPhotoUrl(teamId));

        return teamRepository.save(team);
    }

    @Override
    public void deleteTeamPhoto(Long teamId) throws Exception {
        Team team = getTeamById(teamId);

        User currentUser = securityUtils.getCurrentUser();
        if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the team creator can delete the team photo");
        }

        if (team.getTeamPhotoFileName() == null) {
            throw new RuntimeException("Team has no photo to delete");
        }

        fileStorageService.deleteTeamPhoto(team.getTeamPhotoFileName());

        team.setTeamPhotoFileName(null);
        team.setTeamPhotoContentType(null);
        team.setTeamPhotoSize(null);
        team.setTeamPhotoPath(null);
        team.setTeamPhotoUrl(null);

        teamRepository.save(team);
    }
}