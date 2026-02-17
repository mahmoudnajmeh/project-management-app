package com.example.project_management_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatFileUploadController {

    private final String uploadDir = "uploads/chat/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<Map<String, String>> fileInfo = new ArrayList<>();

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String originalFilename = file.getOriginalFilename();
                String timestamp = String.valueOf(System.currentTimeMillis());
                String storedFilename = timestamp + "_" + originalFilename;

                Path filePath = uploadPath.resolve(storedFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                Map<String, String> fileData = new HashMap<>();
                fileData.put("storedName", storedFilename);
                fileData.put("originalName", originalFilename);

                fileInfo.add(fileData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("files", fileInfo);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading files");
        }
    }

    @GetMapping("/file/{storedName}")
    public ResponseEntity<Resource> getFile(@PathVariable String storedName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(storedName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = getContentType(storedName);
                String originalFilename = storedName.substring(storedName.indexOf("_") + 1);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + originalFilename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            // Microsoft Office
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            // Adobe PDF
            case "pdf": return "application/pdf";

            // Images
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "svg": return "image/svg+xml";

            // Text
            case "txt": return "text/plain";
            case "csv": return "text/csv";
            case "xml": return "application/xml";
            case "json": return "application/json";

            // Archives
            case "zip": return "application/zip";
            case "rar": return "application/x-rar-compressed";
            case "7z": return "application/x-7z-compressed";
            case "tar": return "application/x-tar";
            case "gz": return "application/gzip";

            // Code
            case "java": return "text/x-java-source";
            case "py": return "text/x-python";
            case "js": return "application/javascript";
            case "ts": return "application/typescript";
            case "html": return "text/html";
            case "css": return "text/css";
            case "php": return "text/x-php";
            case "cpp": return "text/x-c++src";
            case "c": return "text/x-csrc";
            case "h": return "text/x-chdr";
            case "class": return "application/java-vm";
            case "jar": return "application/java-archive";

            // Default
            default: return "application/octet-stream";
        }
    }
}