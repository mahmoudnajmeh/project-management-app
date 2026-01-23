package com.example.project_management_app.dto;

public class ProfilePictureResponse {
    private String message;
    private String fileName;
    private String fileUrl;

    public ProfilePictureResponse() {}

    public ProfilePictureResponse(String message, String fileName, String fileUrl) {
        this.message = message;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}