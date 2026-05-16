package com.example.project_management_app.controller;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.service.FileStorageService;
import com.example.project_management_app.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/my-teams")
    public ResponseEntity<List<Team>> getMyTeams() {
        List<Team> teams = teamService.getMyTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Team>> getTeamsByUser(@PathVariable Long userId) {
        List<Team> teams = teamService.getTeamsByUser(userId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeam(@PathVariable Long id) {
        Team team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Team team = teamService.createTeam(name, description);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");

            Team team = teamService.updateTeam(id, name, description);
            return ResponseEntity.ok(team);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        try {
            teamService.deleteTeam(id);
            return ResponseEntity.ok(Map.of("message", "Team deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // Member management endpoints
    @GetMapping("/{teamId}/members")
    public ResponseEntity<?> getTeamMembers(@PathVariable Long teamId) {
        try {
            List<com.example.project_management_app.entity.User> members = teamService.getTeamMembers(teamId);
            return ResponseEntity.ok(members);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<?> addMemberToTeam(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        try {
            Team team = teamService.addMemberToTeam(teamId, userId);
            return ResponseEntity.ok(team);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<?> removeMemberFromTeam(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        try {
            Team team = teamService.removeMemberFromTeam(teamId, userId);
            return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{teamId}/members/{userId}/check")
    public ResponseEntity<Map<String, Boolean>> checkUserInTeam(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        boolean isInTeam = teamService.isUserInTeam(teamId, userId);
        return ResponseEntity.ok(Map.of("isInTeam", isInTeam));
    }

    // Team photo endpoints
    @PostMapping("/{teamId}/photo")
    public ResponseEntity<?> uploadTeamPhoto(
            @PathVariable Long teamId,
            @RequestParam("file") MultipartFile file) {
        try {
            Team updatedTeam = teamService.updateTeamPhoto(teamId, file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Team photo uploaded successfully");
            response.put("fileName", updatedTeam.getTeamPhotoFileName());
            response.put("fileUrl", updatedTeam.getTeamPhotoUrl());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{teamId}/photo")
    public ResponseEntity<byte[]> getTeamPhoto(@PathVariable Long teamId) {
        try {
            Team team = teamService.getTeamById(teamId);

            if (team.getTeamPhotoFileName() == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] image = fileStorageService.loadTeamPhoto(team.getTeamPhotoFileName());

            // Determine content type based on file extension
            String fileName = team.getTeamPhotoFileName();
            String contentType = "image/jpeg";
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (fileName.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\"")
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{teamId}/photo")
    public ResponseEntity<?> deleteTeamPhoto(@PathVariable Long teamId) {
        try {
            teamService.deleteTeamPhoto(teamId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Team photo deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{teamId}/photo/debug")
    public ResponseEntity<?> debugTeamPhoto(@PathVariable Long teamId) {
        try {
            Team team = teamService.getTeamById(teamId);

            Map<String, Object> debug = new HashMap<>();
            debug.put("teamId", teamId);
            debug.put("teamPhotoFileName", team.getTeamPhotoFileName());
            debug.put("teamPhotoUrl", team.getTeamPhotoUrl());
            debug.put("teamPhotoPath", team.getTeamPhotoPath());

            if (team.getTeamPhotoFileName() != null) {
                try {
                    byte[] image = fileStorageService.loadTeamPhoto(team.getTeamPhotoFileName());
                    debug.put("fileExists", true);
                    debug.put("fileSize", image.length);

                    // Check file extension
                    String fileName = team.getTeamPhotoFileName();
                    debug.put("fileName", fileName);
                    debug.put("hasExtension", fileName.contains("."));
                    if (fileName.contains(".")) {
                        debug.put("extension", fileName.substring(fileName.lastIndexOf(".")));
                    }
                } catch (IOException e) {
                    debug.put("fileExists", false);
                    debug.put("error", e.getMessage());
                }
            }

            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}