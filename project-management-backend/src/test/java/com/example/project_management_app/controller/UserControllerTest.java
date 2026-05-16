package com.example.project_management_app.controller;

import com.example.project_management_app.dto.InvitationRequest;
import com.example.project_management_app.dto.PasswordChangeRequest;
import com.example.project_management_app.dto.UserDto;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.EmailService;
import com.example.project_management_app.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User currentUser;
    private User adminUser;
    private UserDto userDto;
    private PasswordChangeRequest passwordChangeRequest;
    private InvitationRequest invitationRequest;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("Mahmoud");
        currentUser.setEmail("mn.de@outlook.com");
        currentUser.setFirstName("Mahmoud");
        currentUser.setLastName("Najmeh");
        currentUser.setRole(User.Role.ROLE_USER);
        currentUser.setProfilePictureFileName("user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg");
        currentUser.setProfilePictureContentType("image/jpeg");
        currentUser.setProfilePicturePath("./uploads/profile-pictures/user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(User.Role.ROLE_ADMIN);

        userDto = new UserDto();
        userDto.setUsername("MahmoudUpdated");
        userDto.setEmail("mn.updated@outlook.com");
        userDto.setFirstName("Mahmoud");
        userDto.setLastName("NajmehUpdated");
        userDto.setPassword("NewPassword123!");

        passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("OldPassword123!");
        passwordChangeRequest.setNewPassword("NewPassword123!");
        passwordChangeRequest.setConfirmPassword("NewPassword123!");

        invitationRequest = new InvitationRequest();
        invitationRequest.setEmail("newuser@example.com");
        invitationRequest.setRole("ROLE_USER");

        userList = Arrays.asList(currentUser, adminUser);
    }

    @Test
    void getCurrentUserSuccess() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Mahmoud"))
                .andExpect(jsonPath("$.email").value("mn.de@outlook.com"))
                .andExpect(jsonPath("$.firstName").value("Mahmoud"))
                .andExpect(jsonPath("$.lastName").value("Najmeh"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(accountService, times(1)).getCurrentUser();
    }

    @Test
    void getCurrentUserError() throws Exception {
        when(accountService.getCurrentUser()).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateCurrentUserSuccess() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("MahmoudUpdated");
        updatedUser.setEmail("mn.updated@outlook.com");
        updatedUser.setFirstName("Mahmoud");
        updatedUser.setLastName("NajmehUpdated");

        when(accountService.updateUser(any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("MahmoudUpdated"))
                .andExpect(jsonPath("$.email").value("mn.updated@outlook.com"))
                .andExpect(jsonPath("$.lastName").value("NajmehUpdated"));

        verify(accountService, times(1)).updateUser(any(UserDto.class));
    }

    @Test
    void updateCurrentUserBadRequest() throws Exception {
        when(accountService.updateUser(any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid user data"));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUserRuntimeError() throws Exception {
        when(accountService.updateUser(any(UserDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteCurrentUserSuccess() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(accountService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(accountService, times(1)).getCurrentUser();
        verify(accountService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteCurrentUserNotFound() throws Exception {
        when(accountService.getCurrentUser()).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void uploadProfilePictureSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setProfilePictureFileName("user_1_new.jpg");
        updatedUser.setProfilePictureUrl("/api/users/profile-picture/user_1_new.jpg");

        when(accountService.updateProfilePicture(any(MultipartFile.class))).thenReturn(updatedUser);

        mockMvc.perform(multipart("/api/users/me/profile-picture")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile picture uploaded successfully"))
                .andExpect(jsonPath("$.fileName").value("user_1_new.jpg"))
                .andExpect(jsonPath("$.fileUrl").value("/api/users/profile-picture/user_1_new.jpg"));

        verify(accountService, times(1)).updateProfilePicture(any(MultipartFile.class));
    }

    @Test
    void uploadProfilePictureBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(accountService.updateProfilePicture(any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Invalid file format"));

        mockMvc.perform(multipart("/api/users/me/profile-picture")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid file format"));
    }

    @Test
    void getMyProfilePictureSuccess() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);

        byte[] imageData = "fake image data".getBytes();
        when(fileStorageService.loadProfilePicture(currentUser.getProfilePictureFileName()))
                .thenReturn(imageData);

        mockMvc.perform(get("/api/users/me/profile-picture"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg\""))
                .andExpect(content().bytes(imageData));

        verify(accountService, times(1)).getCurrentUser();
        verify(fileStorageService, times(1)).loadProfilePicture(anyString());
    }

    @Test
    void getMyProfilePictureNotFound() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);

        when(fileStorageService.loadProfilePicture(anyString()))
                .thenThrow(new IOException("File not found"));

        mockMvc.perform(get("/api/users/me/profile-picture"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyProfilePictureNoPicture() throws Exception {
        User userWithNoPicture = new User();
        userWithNoPicture.setId(1L);
        userWithNoPicture.setProfilePictureFileName(null);

        when(accountService.getCurrentUser()).thenReturn(userWithNoPicture);

        mockMvc.perform(get("/api/users/me/profile-picture"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfilePictureByFilenameSuccess() throws Exception {
        String filename = "user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg";
        byte[] imageData = "fake image data".getBytes();

        when(fileStorageService.loadProfilePicture(filename)).thenReturn(imageData);

        mockMvc.perform(get("/api/users/profile-picture/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\""))
                .andExpect(content().bytes(imageData));

        verify(fileStorageService, times(1)).loadProfilePicture(filename);
    }

    @Test
    void getProfilePicturePngSuccess() throws Exception {
        String filename = "user_1_test.png";
        byte[] imageData = "fake image data".getBytes();

        when(fileStorageService.loadProfilePicture(filename)).thenReturn(imageData);

        mockMvc.perform(get("/api/users/profile-picture/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(content().bytes(imageData));
    }

    @Test
    void getProfilePictureGifSuccess() throws Exception {
        String filename = "user_1_test.gif";
        byte[] imageData = "fake image data".getBytes();

        when(fileStorageService.loadProfilePicture(filename)).thenReturn(imageData);

        mockMvc.perform(get("/api/users/profile-picture/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/gif"))
                .andExpect(content().bytes(imageData));
    }

    @Test
    void getProfilePictureNotFound() throws Exception {
        String filename = "nonexistent.jpg";

        when(fileStorageService.loadProfilePicture(filename))
                .thenThrow(new IOException("File not found"));

        mockMvc.perform(get("/api/users/profile-picture/{filename}", filename))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProfilePictureSuccess() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(fileStorageService).deleteProfilePicture(anyString());
        when(accountService.updateUser(any(User.class))).thenReturn(currentUser);

        mockMvc.perform(delete("/api/users/me/profile-picture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile picture deleted successfully"));

        verify(fileStorageService, times(1)).deleteProfilePicture(anyString());
        verify(accountService, times(1)).updateUser(any(User.class));
    }

    @Test
    void deleteProfilePictureNoPicture() throws Exception {
        User userWithNoPicture = new User();
        userWithNoPicture.setId(1L);
        userWithNoPicture.setProfilePictureFileName(null);

        when(accountService.getCurrentUser()).thenReturn(userWithNoPicture);

        mockMvc.perform(delete("/api/users/me/profile-picture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No profile picture to delete"));
    }

    @Test
    void deleteProfilePictureIOError() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(currentUser);
        doThrow(new IOException("Failed to delete"))
                .when(fileStorageService).deleteProfilePicture(anyString());

        mockMvc.perform(delete("/api/users/me/profile-picture"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to delete profile picture"));
    }

    @Test
    void getAllUsersSuccess() throws Exception {
        when(userRepository.findAll()).thenReturn(userList);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("Mahmoud"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("admin"));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void sendInvitationSuccess() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.existsByEmail(invitationRequest.getEmail())).thenReturn(false);
        when(emailService.generateInvitationToken(anyString())).thenReturn("test-token");
        doNothing().when(emailService).sendInvitationEmail(anyString(), any(), anyString(), anyString());

        mockMvc.perform(post("/api/users/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitation sent successfully"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(emailService, times(1)).sendInvitationEmail(anyString(), any(), anyString(), anyString());
    }

    @Test
    void sendInvitationUserAlreadyExists() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.existsByEmail(invitationRequest.getEmail())).thenReturn(true);

        mockMvc.perform(post("/api/users/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with this email already exists"));
    }

    @Test
    void sendInvitationError() throws Exception {
        when(accountService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.existsByEmail(invitationRequest.getEmail())).thenReturn(false);
        when(emailService.generateInvitationToken(anyString()))
                .thenThrow(new RuntimeException("Email service error"));

        mockMvc.perform(post("/api/users/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Failed to send invitation: Email service error"));
    }

    @Test
    void changePasswordSuccess() throws Exception {
        doNothing().when(accountService).changePassword(any(PasswordChangeRequest.class));

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(accountService, times(1)).changePassword(any(PasswordChangeRequest.class));
    }

    @Test
    void changePasswordBadRequest() throws Exception {
        doThrow(new RuntimeException("Current password is incorrect"))
                .when(accountService).changePassword(any(PasswordChangeRequest.class));

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is incorrect"));
    }

    @Test
    void changePasswordValidationError() throws Exception {
        PasswordChangeRequest invalidRequest = new PasswordChangeRequest();
        invalidRequest.setCurrentPassword("old");
        invalidRequest.setNewPassword("new");
        invalidRequest.setConfirmPassword("different");

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}