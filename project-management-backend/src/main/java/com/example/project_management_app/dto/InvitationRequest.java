package com.example.project_management_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class InvitationRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String role;

    public InvitationRequest() {}

    public InvitationRequest(String email, String role) {
        this.email = email;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}