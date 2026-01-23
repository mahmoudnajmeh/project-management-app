package com.example.project_management_app.service;

import com.example.project_management_app.dto.LoginRequest;
import com.example.project_management_app.dto.UserDto;
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
}