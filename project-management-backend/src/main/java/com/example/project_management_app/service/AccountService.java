package com.example.project_management_app.service;

import com.example.project_management_app.dto.*;
import com.example.project_management_app.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {
    User registerUser(UserDto userDto);
    String loginUser(LoginRequest loginRequest);
    User getCurrentUser();
    User updateUser(UserDto userDto);
    User updateUser(User user);
    void deleteUser(Long userId);
    User updateProfilePicture(MultipartFile file) throws Exception;
    void changePassword(PasswordChangeRequest passwordRequest);
    void sendPasswordResetEmail(ForgotPasswordRequest request) throws Exception;
    void resetPassword(ResetPasswordRequest request);
    boolean validateResetToken(String token);
    User processOAuthLogin(String email, String name, String provider);
}