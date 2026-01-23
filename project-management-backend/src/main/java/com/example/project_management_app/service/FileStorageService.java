package com.example.project_management_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.profile-pictures-dir}")
    private String profilePicturesDir;

    public String storeProfilePicture(MultipartFile file, Long userId) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(profilePicturesDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = "user_" + userId + "_" + UUID.randomUUID() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

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

    public void deleteProfilePicture(String filename) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            Path filePath = Paths.get(profilePicturesDir).resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }

    public String getProfilePictureUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return "/api/users/profile-picture/" + filename;
    }
}