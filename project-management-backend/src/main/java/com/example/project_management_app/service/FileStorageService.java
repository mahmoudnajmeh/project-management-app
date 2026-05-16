package com.example.project_management_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.profile-pictures-dir}")
    private String profilePicturesDir;

    @Value("${file.team-photos-dir:./uploads/team-photos}")
    private String teamPhotosDir;

    public String storeProfilePicture(MultipartFile file, Long userId) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(profilePicturesDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get file extension
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generate unique filename with extension
        String uniqueFileName = "user_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public String storeTeamPhoto(MultipartFile file, Long teamId) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(teamPhotosDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get file extension
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generate unique filename with extension
        String uniqueFileName = "team_" + teamId + "_" + UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public byte[] loadProfilePicture(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IOException("Filename is empty");
        }
        Path filePath = Paths.get(profilePicturesDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }
        return Files.readAllBytes(filePath);
    }

    public byte[] loadTeamPhoto(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IOException("Filename is empty");
        }
        Path filePath = Paths.get(teamPhotosDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }
        return Files.readAllBytes(filePath);
    }

    public void deleteProfilePicture(String filename) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            Path filePath = Paths.get(profilePicturesDir).resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }

    public void deleteTeamPhoto(String filename) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            Path filePath = Paths.get(teamPhotosDir).resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }

    public String getProfilePictureUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return "/api/users/profile-picture/" + filename;
    }

    public String getTeamPhotoUrl(Long teamId) {
        return "/api/teams/" + teamId + "/photo";
    }
}