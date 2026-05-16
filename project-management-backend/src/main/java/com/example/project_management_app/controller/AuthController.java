package com.example.project_management_app.controller;

import com.example.project_management_app.dto.ForgotPasswordRequest;
import com.example.project_management_app.dto.LoginRequest;
import com.example.project_management_app.dto.ResetPasswordRequest;
import com.example.project_management_app.dto.UserDto;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }

            String errorMessage = errors.values().stream()
                    .collect(Collectors.joining(", "));

            logger.error("Validation errors: {}", errors);

            Map<String, String> response = new HashMap<>();
            response.put("error", errorMessage);
            response.put("validationErrors", errors.toString());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            logger.info("Registering user: {}", userDto.getUsername());
            User user = accountService.registerUser(userDto);
            logger.info("User registered successfully: {}", user.getUsername());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Registration error: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register-with-token")
    public ResponseEntity<?> registerWithToken(@Valid @RequestBody UserDto userDto,
                                               @RequestParam String token,
                                               BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }

            String errorMessage = errors.values().stream()
                    .collect(Collectors.joining(", "));

            logger.error("Validation errors: {}", errors);

            Map<String, String> response = new HashMap<>();
            response.put("error", errorMessage);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (!emailService.validateInvitationToken(token)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid or expired invitation token");
                return ResponseEntity.badRequest().body(response);
            }

            String invitedEmail = emailService.getEmailFromToken(token);
            if (!invitedEmail.equals(userDto.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Email does not match invitation");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Registering user with invitation token: {}", userDto.getUsername());
            User user = accountService.registerUser(userDto);
            logger.info("User registered successfully with invitation: {}", user.getUsername());

            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Registration with token error: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }

            String errorMessage = errors.values().stream()
                    .collect(Collectors.joining(", "));

            logger.error("Login validation errors: {}", errors);

            Map<String, String> response = new HashMap<>();
            response.put("error", errorMessage);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            logger.info("Login attempt for user: {}", loginRequest.getUsername());
            String token = accountService.loginUser(loginRequest);
            logger.info("Login successful for user: {}", loginRequest.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", loginRequest.getUsername());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Login error for user {}: {}", loginRequest.getUsername(), e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            logger.error("Unexpected login error: {}", e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Auth endpoint is working");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        System.out.println("=== FORGOT PASSWORD ENDPOINT CALLED ===");
        System.out.println("Request email: " + request.getEmail());

        try {
            accountService.sendPasswordResetEmail(request);
            System.out.println("Password reset email process completed");
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account exists with this email, you will receive a password reset link.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("Runtime error: " + e.getMessage());
            // Don't reveal if email exists or not for security
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account exists with this email, you will receive a password reset link.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to send reset email. Please try again later.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            accountService.resetPassword(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password has been reset successfully. You can now login with your new password.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = accountService.validateResetToken(token);
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
}