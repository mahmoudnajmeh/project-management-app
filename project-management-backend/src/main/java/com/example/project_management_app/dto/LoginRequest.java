package com.example.project_management_app.dto;

public class LoginRequest {
    private String username;
    private String password;

    // Constructors, Getters, and Setters
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}