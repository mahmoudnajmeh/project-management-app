package com.example.project_management_app.controller;

import com.example.project_management_app.dto.InvitationRequest;
import com.example.project_management_app.dto.UserDto;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.EmailService;
import com.example.project_management_app.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = accountService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody UserDto userDto) {
        User user = accountService.updateUser(userDto);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteCurrentUser() {
        User currentUser = accountService.getCurrentUser();
        accountService.deleteUser(currentUser.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            User updatedUser = accountService.updateProfilePicture(file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("fileName", updatedUser.getProfilePictureFileName());
            response.put("fileUrl", updatedUser.getProfilePictureUrl());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/me/profile-picture")
    public ResponseEntity<byte[]> getMyProfilePicture() {
        try {
            User currentUser = accountService.getCurrentUser();

            if (currentUser.getProfilePictureFileName() == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] image = fileStorageService.loadProfilePicture(currentUser.getProfilePictureFileName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(currentUser.getProfilePictureContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + currentUser.getProfilePictureFileName() + "\"")
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile-picture/{filename}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String filename) {
        try {
            byte[] image = fileStorageService.loadProfilePicture(filename);

            String contentType = "image/jpeg";
            if (filename.endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<?> deleteProfilePicture() {
        try {
            User currentUser = accountService.getCurrentUser();

            if (currentUser.getProfilePictureFileName() == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "No profile picture to delete");
                return ResponseEntity.ok(response);
            }

            fileStorageService.deleteProfilePicture(currentUser.getProfilePictureFileName());

            currentUser.setProfilePictureFileName(null);
            currentUser.setProfilePictureContentType(null);
            currentUser.setProfilePictureSize(null);
            currentUser.setProfilePicturePath(null);
            currentUser.setProfilePictureUrl(null);

            accountService.updateUser(currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete profile picture");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendInvitation(@RequestBody InvitationRequest invitationRequest) {
        try {
            User currentUser = accountService.getCurrentUser();

            if (userRepository.existsByEmail(invitationRequest.getEmail())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User with this email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            String invitationToken = emailService.generateInvitationToken(invitationRequest.getEmail());
            emailService.sendInvitationEmail(
                    invitationRequest.getEmail(),
                    invitationRequest.getRole(),
                    currentUser.getUsername(),
                    invitationToken
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Invitation sent successfully");
            response.put("email", invitationRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to send invitation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}