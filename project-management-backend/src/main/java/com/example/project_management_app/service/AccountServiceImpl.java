package com.example.project_management_app.service;

import com.example.project_management_app.dto.LoginRequest;
import com.example.project_management_app.dto.UserDto;
import com.example.project_management_app.entity.User;
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

    public void updateUserActivity(Long userId) {
        userRepository.updateLastActivity(userId, LocalDateTime.now());
    }
}