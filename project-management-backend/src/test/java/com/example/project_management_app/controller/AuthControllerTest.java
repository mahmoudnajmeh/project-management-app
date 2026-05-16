package com.example.project_management_app.controller;

import com.example.project_management_app.dto.LoginRequest;
import com.example.project_management_app.dto.UserDto;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private UserDto validUserDto;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        validUserDto = new UserDto();
        validUserDto.setUsername("Mahmoud");
        validUserDto.setEmail("mn.de@outlook.com");
        validUserDto.setPassword("Password123!");
        validUserDto.setFirstName("Mahmoud");
        validUserDto.setLastName("Najmeh");
        validUserDto.setRole(User.Role.ROLE_USER);

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("Mahmoud");
        validLoginRequest.setPassword("Password123!");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Mahmoud");
        testUser.setEmail("mn.de@outlook.com");
        testUser.setFirstName("Mahmoud");
        testUser.setLastName("Najmeh");
        testUser.setRole(User.Role.ROLE_USER);
    }

    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth endpoint is working"));
    }

    @Test
    void registerSuccess() throws Exception {
        when(accountService.registerUser(any(UserDto.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Mahmoud"))
                .andExpect(jsonPath("$.email").value("mn.de@outlook.com"));

        verify(accountService, times(1)).registerUser(any(UserDto.class));
    }

    @Test
    void registerValidationFails() throws Exception {
        UserDto invalidUserDto = new UserDto();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerDuplicateUser() throws Exception {
        when(accountService.registerUser(any(UserDto.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void registerWeakPassword() throws Exception {
        UserDto weakPasswordUser = new UserDto();
        weakPasswordUser.setUsername("testuser");
        weakPasswordUser.setEmail("test@example.com");
        weakPasswordUser.setPassword("weak");
        weakPasswordUser.setFirstName("Test");
        weakPasswordUser.setLastName("User");
        weakPasswordUser.setRole(User.Role.ROLE_USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPasswordUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithValidToken() throws Exception {
        String validToken = "valid-token-123";

        when(emailService.validateInvitationToken(validToken)).thenReturn(true);
        when(emailService.getEmailFromToken(validToken)).thenReturn(validUserDto.getEmail());
        when(accountService.registerUser(any(UserDto.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register-with-token")
                        .param("token", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Mahmoud"));

        verify(emailService, times(1)).validateInvitationToken(validToken);
        verify(emailService, times(1)).getEmailFromToken(validToken);
        verify(accountService, times(1)).registerUser(any(UserDto.class));
    }

    @Test
    void registerWithInvalidToken() throws Exception {
        String invalidToken = "invalid-token";

        when(emailService.validateInvitationToken(invalidToken)).thenReturn(false);

        mockMvc.perform(post("/api/auth/register-with-token")
                        .param("token", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or expired invitation token"));

        verify(accountService, never()).registerUser(any(UserDto.class));
    }

    @Test
    void registerWithTokenEmailMismatch() throws Exception {
        String validToken = "valid-token-123";
        String differentEmail = "different@email.com";

        when(emailService.validateInvitationToken(validToken)).thenReturn(true);
        when(emailService.getEmailFromToken(validToken)).thenReturn(differentEmail);

        mockMvc.perform(post("/api/auth/register-with-token")
                        .param("token", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email does not match invitation"));

        verify(accountService, never()).registerUser(any(UserDto.class));
    }

    @Test
    void loginSuccess() throws Exception {
        String expectedToken = "jwt-token-123";
        when(accountService.loginUser(any(LoginRequest.class))).thenReturn(expectedToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken))
                .andExpect(jsonPath("$.username").value("Mahmoud"))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(accountService, times(1)).loginUser(any(LoginRequest.class));
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        when(accountService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void loginMissingFields() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginServiceError() throws Exception {
        when(accountService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
}