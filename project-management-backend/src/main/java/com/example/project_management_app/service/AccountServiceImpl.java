package com.example.project_management_app.service;

import com.example.project_management_app.dto.*;
import com.example.project_management_app.entity.PasswordResetToken;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.PasswordResetTokenRepository;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public User registerUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(User.Role.ROLE_USER);
        user.setLastActivity(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public String loginUser(LoginRequest loginRequest) {
        String token = authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastActivity(LocalDateTime.now());
        userRepository.save(user);

        return token;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateUser(UserDto userDto) {
        User currentUser = getCurrentUser();

        if (userDto.getFirstName() != null) {
            currentUser.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            currentUser.setLastName(userDto.getLastName());
        }

        // ADD THIS BLOCK - Allow username change
        if (userDto.getUsername() != null && !userDto.getUsername().equals(currentUser.getUsername())) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new RuntimeException("Username is already taken");
            }
            currentUser.setUsername(userDto.getUsername());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }
            currentUser.setEmail(userDto.getEmail());
        }

        currentUser.setLastActivity(LocalDateTime.now());
        return userRepository.save(currentUser);
    }

    @Override
    public User updateUser(User user) {
        user.setLastActivity(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own account");
        }

        if (currentUser.getProfilePictureFileName() != null) {
            try {
                fileStorageService.deleteProfilePicture(currentUser.getProfilePictureFileName());
            } catch (IOException e) {
                System.err.println("Failed to delete profile picture: " + e.getMessage());
            }
        }

        userRepository.deleteById(userId);
    }

    @Override
    public User updateProfilePicture(MultipartFile file) throws Exception {
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

        User currentUser = getCurrentUser();

        if (currentUser.getProfilePictureFileName() != null) {
            try {
                fileStorageService.deleteProfilePicture(currentUser.getProfilePictureFileName());
            } catch (IOException e) {
                System.err.println("Failed to delete old profile picture: " + e.getMessage());
            }
        }

        String fileName = fileStorageService.storeProfilePicture(file, currentUser.getId());

        currentUser.setProfilePictureFileName(fileName);
        currentUser.setProfilePictureContentType(contentType);
        currentUser.setProfilePictureSize(file.getSize());
        currentUser.setProfilePicturePath("./uploads/profile-pictures/" + fileName);
        currentUser.setProfilePictureUrl(fileStorageService.getProfilePictureUrl(fileName));
        currentUser.setLastActivity(LocalDateTime.now());

        return userRepository.save(currentUser);
    }

    @Override
    public void changePassword(PasswordChangeRequest passwordRequest) {
        User currentUser = getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new password and confirm password match
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Check if new password is same as old password
        if (passwordEncoder.matches(passwordRequest.getNewPassword(), currentUser.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Encode and set new password
        currentUser.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        currentUser.setLastActivity(LocalDateTime.now());

        userRepository.save(currentUser);
    }

    @Override
    public void sendPasswordResetEmail(ForgotPasswordRequest request) throws Exception {
        System.out.println("=== PASSWORD RESET REQUEST ===");
        System.out.println("Looking for email: " + request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.out.println("❌ Email not found: " + request.getEmail());
                    return new RuntimeException("No user found with this email address");
                });

        System.out.println("✅ User found: " + user.getEmail());
        System.out.println("User name: " + user.getFirstName() + " " + user.getLastName());

        // Delete any existing reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);
        System.out.println("Deleted existing reset tokens");

        // Generate new token
        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);
        System.out.println("Generated new reset token: " + token);

        // Send email
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        System.out.println("Reset link: " + resetLink);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink, user.getFirstName());
            System.out.println("✅ Email sending completed");
        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public boolean validateResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    @Override
    public User processOAuthLogin(String email, String name, String provider) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setLastActivity(LocalDateTime.now());
                    if (user.getProvider() == null) {
                        user.setProvider(provider);
                    }
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email.split("@")[0] + "_" + provider);
                    newUser.setFirstName(name.split(" ")[0]);
                    newUser.setLastName(name.split(" ").length > 1 ? name.split(" ")[1] : "");
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(User.Role.ROLE_USER);
                    newUser.setProvider(provider);
                    newUser.setLastActivity(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }

    @Override
    public void updateUserActivity(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
