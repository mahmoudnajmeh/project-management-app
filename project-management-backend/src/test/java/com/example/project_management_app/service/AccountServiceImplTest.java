package com.example.project_management_app.service;

import com.example.project_management_app.dto.LoginRequest;
import com.example.project_management_app.dto.PasswordChangeRequest;
import com.example.project_management_app.dto.UserDto;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private UserDto userDto;
    private LoginRequest loginRequest;
    private PasswordChangeRequest passwordChangeRequest;
    private MultipartFile profilePicture;
    private final String username = "Mahmoud";
    private final String password = "password123";
    private final String encodedPassword = "$2a$10$GMSdL3Sq7mLftnaa1gsS9.a.H4g1q4mwo/NIR8jOsYowiI5XYUon2";
    private final String newPassword = "NewPassword123!";
    private final String token = "eyJhbGciOiJIUzI1NiJ9.token";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setRole(User.Role.ROLE_USER);
        user.setLastActivity(LocalDateTime.now());

        userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setEmail("new@example.com");
        userDto.setPassword(password);
        userDto.setFirstName("New");
        userDto.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword(password);
        passwordChangeRequest.setNewPassword(newPassword);
        passwordChangeRequest.setConfirmPassword(newPassword);

        profilePicture = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void registerUserSuccess() {
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = accountService.registerUser(userDto);

        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertEquals("mn.de@outlook.com", registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPassword());

        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(passwordEncoder, times(1)).encode(userDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUserUsernameTaken() {
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.registerUser(userDto);
        });

        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserEmailTaken() {
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.registerUser(userDto);
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUserSuccess() {
        when(authenticationService.authenticate(username, password)).thenReturn(token);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        String result = accountService.loginUser(loginRequest);

        assertNotNull(result);
        assertEquals(token, result);

        verify(authenticationService, times(1)).authenticate(username, password);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void loginUserNotFound() {
        when(authenticationService.authenticate(username, password)).thenReturn(token);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.loginUser(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(authenticationService, times(1)).authenticate(username, password);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUserAuthenticationFails() {
        when(authenticationService.authenticate(username, password)).thenThrow(new RuntimeException("Invalid credentials"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.loginUser(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authenticationService, times(1)).authenticate(username, password);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentUserSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User currentUser = accountService.getCurrentUser();

        assertNotNull(currentUser);
        assertEquals(1L, currentUser.getId());
        assertEquals(username, currentUser.getUsername());

        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getCurrentUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getCurrentUser();
        });

        assertEquals("User not found", exception.getMessage());
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void updateUserWithUserDtoSuccess() {
        UserDto updateDto = new UserDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");
        updateDto.setEmail("updated@example.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = accountService.updateUser(updateDto);

        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertEquals("updated@example.com", updatedUser.getEmail());

        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).existsByEmail("updated@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserWithUserDtoPartialUpdate() {
        UserDto updateDto = new UserDto();
        updateDto.setFirstName("Updated Only");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = accountService.updateUser(updateDto);

        assertNotNull(updatedUser);
        assertEquals("Updated Only", updatedUser.getFirstName());
        assertEquals("Najmeh", updatedUser.getLastName());
        assertEquals("mn.de@outlook.com", updatedUser.getEmail());

        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserWithUserDtoEmailAlreadyExists() {
        UserDto updateDto = new UserDto();
        updateDto.setEmail("existing@example.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.updateUser(updateDto);
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserWithUserObjectSuccess() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = accountService.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());

        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void deleteUserSuccess() throws IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> accountService.deleteUser(1L));

        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).deleteById(1L);
        verify(fileStorageService, never()).deleteProfilePicture(anyString());
    }

    @Test
    void deleteUserWithProfilePictureSuccess() throws IOException {
        user.setProfilePictureFileName("profile.jpg");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doNothing().when(fileStorageService).deleteProfilePicture("profile.jpg");
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> accountService.deleteUser(1L));

        verify(userRepository, times(1)).findByUsername(username);
        verify(fileStorageService, times(1)).deleteProfilePicture("profile.jpg");
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserWithProfilePictureDeleteFails() throws IOException {
        user.setProfilePictureFileName("profile.jpg");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doThrow(new IOException("Failed to delete")).when(fileStorageService).deleteProfilePicture("profile.jpg");
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> accountService.deleteUser(1L));

        verify(userRepository, times(1)).findByUsername(username);
        verify(fileStorageService, times(1)).deleteProfilePicture("profile.jpg");
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserDifferentId() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteUser(2L);
        });

        assertEquals("You can only delete your own account", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateProfilePictureSuccess() throws Exception {
        String fileName = "user_1_new.jpg";
        String fileUrl = "/api/users/profile-picture/user_1_new.jpg";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(fileStorageService.storeProfilePicture(any(MultipartFile.class), eq(1L))).thenReturn(fileName);
        when(fileStorageService.getProfilePictureUrl(fileName)).thenReturn(fileUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = accountService.updateProfilePicture(profilePicture);

        assertNotNull(updatedUser);
        assertEquals(fileName, updatedUser.getProfilePictureFileName());
        assertEquals("image/jpeg", updatedUser.getProfilePictureContentType());
        assertEquals(fileUrl, updatedUser.getProfilePictureUrl());

        verify(userRepository, times(1)).findByUsername(username);
        verify(fileStorageService, times(1)).storeProfilePicture(any(MultipartFile.class), eq(1L));
        verify(fileStorageService, times(1)).getProfilePictureUrl(fileName);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfilePictureWithExistingPicture() throws Exception {
        user.setProfilePictureFileName("old_picture.jpg");
        String fileName = "user_1_new.jpg";
        String fileUrl = "/api/users/profile-picture/user_1_new.jpg";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doNothing().when(fileStorageService).deleteProfilePicture("old_picture.jpg");
        when(fileStorageService.storeProfilePicture(any(MultipartFile.class), eq(1L))).thenReturn(fileName);
        when(fileStorageService.getProfilePictureUrl(fileName)).thenReturn(fileUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = accountService.updateProfilePicture(profilePicture);

        assertNotNull(updatedUser);
        assertEquals(fileName, updatedUser.getProfilePictureFileName());

        verify(fileStorageService, times(1)).deleteProfilePicture("old_picture.jpg");
        verify(fileStorageService, times(1)).storeProfilePicture(any(MultipartFile.class), eq(1L));
    }

    @Test
    void updateProfilePictureWithExistingPictureDeleteFails() throws Exception {
        user.setProfilePictureFileName("old_picture.jpg");
        String fileName = "user_1_new.jpg";
        String fileUrl = "/api/users/profile-picture/user_1_new.jpg";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doThrow(new IOException("Failed to delete")).when(fileStorageService).deleteProfilePicture("old_picture.jpg");
        when(fileStorageService.storeProfilePicture(any(MultipartFile.class), eq(1L))).thenReturn(fileName);
        when(fileStorageService.getProfilePictureUrl(fileName)).thenReturn(fileUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = accountService.updateProfilePicture(profilePicture);

        assertNotNull(updatedUser);
        assertEquals(fileName, updatedUser.getProfilePictureFileName());

        verify(fileStorageService, times(1)).deleteProfilePicture("old_picture.jpg");
        verify(fileStorageService, times(1)).storeProfilePicture(any(MultipartFile.class), eq(1L));
    }

    @Test
    void updateProfilePictureEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.updateProfilePicture(emptyFile);
        });

        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void updateProfilePictureFileTooLarge() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile largeFile = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeContent);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.updateProfilePicture(largeFile);
        });

        assertEquals("File size exceeds 10MB limit", exception.getMessage());
    }

    @Test
    void updateProfilePictureInvalidContentType() {
        MultipartFile textFile = new MockMultipartFile("file", "text.txt", "text/plain", "content".getBytes());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.updateProfilePicture(textFile);
        });

        assertEquals("Only image files are allowed", exception.getMessage());
    }

    @Test
    void updateProfilePictureNullContentType() {
        MultipartFile nullTypeFile = new MockMultipartFile("file", "file.jpg", null, "content".getBytes());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.updateProfilePicture(nullTypeFile);
        });

        assertEquals("Only image files are allowed", exception.getMessage());
    }

    @Test
    void changePasswordSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> accountService.changePassword(passwordChangeRequest));

        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(passwordEncoder, times(1)).matches(newPassword, encodedPassword);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePasswordCurrentPasswordIncorrect() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.changePassword(passwordChangeRequest);
        });

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePasswordPasswordsDoNotMatch() {
        PasswordChangeRequest mismatchRequest = new PasswordChangeRequest();
        mismatchRequest.setCurrentPassword(password);
        mismatchRequest.setNewPassword(newPassword);
        mismatchRequest.setConfirmPassword("DifferentPassword");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.changePassword(mismatchRequest);
        });

        assertEquals("New password and confirmation do not match", exception.getMessage());
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePasswordSameAsOldPassword() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedPassword)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.changePassword(passwordChangeRequest);
        });

        assertEquals("New password must be different from current password", exception.getMessage());
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(passwordEncoder, times(1)).matches(newPassword, encodedPassword);
        verify(userRepository, never()).save(any(User.class));
    }
}