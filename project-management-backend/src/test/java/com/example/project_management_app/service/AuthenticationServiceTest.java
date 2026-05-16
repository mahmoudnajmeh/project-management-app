package com.example.project_management_app.service;

import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private final String username = "Mahmoud";
    private final String password = "password123";
    private final String encodedPassword = "$2a$10$GMSdL3Sq7mLftnaa1gsS9.a.H4g1q4mwo/NIR8jOsYowiI5XYUon2";
    private final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNYWhtb3VkIiwiaWF0IjoxNzQwMzUyMDAwLCJleHAiOjE3NDA0Mzg0MDB9.token";

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
    }

    @Test
    void loadUserByUsernameSuccess() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = authenticationService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(encodedPassword, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsernameUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.loadUserByUsername(username);
        });

        assertEquals("User not found: " + username, exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsernameWithAdminRole() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword(encodedPassword);
        adminUser.setRole(User.Role.ROLE_ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        UserDetails userDetails = authenticationService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    void authenticateSuccess() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(username)).thenReturn(token);

        String result = authenticationService.authenticate(username, password);

        assertNotNull(result);
        assertEquals(token, result);

        verify(userRepository, times(2)).findByUsername(username); // Called in authenticate and loadUserByUsername
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, times(1)).generateToken(username);
    }

    @Test
    void authenticateUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(username, password);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void authenticateInvalidPassword() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(username, password);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void authenticateWithDifferentUsernameCase() {
        String usernameWithDifferentCase = "mahmoud";

        when(userRepository.findByUsername(usernameWithDifferentCase)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(usernameWithDifferentCase, password);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(usernameWithDifferentCase);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void authenticateWithNullPassword() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(username, null);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(null, encodedPassword);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void authenticateWithEmptyPassword() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("", encodedPassword)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(username, "");
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches("", encodedPassword);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loadUserByUsernameWithSpecialCharacters() {
        String specialUsername = "user@#$%";
        User specialUser = new User();
        specialUser.setId(3L);
        specialUser.setUsername(specialUsername);
        specialUser.setPassword(encodedPassword);
        specialUser.setRole(User.Role.ROLE_USER);

        when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(specialUser));

        UserDetails userDetails = authenticationService.loadUserByUsername(specialUsername);

        assertNotNull(userDetails);
        assertEquals(specialUsername, userDetails.getUsername());
        assertEquals(encodedPassword, userDetails.getPassword());

        verify(userRepository, times(1)).findByUsername(specialUsername);
    }

    @Test
    void authenticateWithSpecialCharacters() {
        String specialUsername = "user@#$%";
        User specialUser = new User();
        specialUser.setId(3L);
        specialUser.setUsername(specialUsername);
        specialUser.setPassword(encodedPassword);
        specialUser.setRole(User.Role.ROLE_USER);

        when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(specialUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(specialUsername)).thenReturn(token);

        String result = authenticationService.authenticate(specialUsername, password);

        assertNotNull(result);
        assertEquals(token, result);

        verify(userRepository, times(2)).findByUsername(specialUsername); // Called in authenticate and loadUserByUsername
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, times(1)).generateToken(specialUsername);
    }

    @Test
    void authenticateWithVeryLongUsername() {
        String longUsername = "a".repeat(100);
        User longUser = new User();
        longUser.setId(4L);
        longUser.setUsername(longUsername);
        longUser.setPassword(encodedPassword);
        longUser.setRole(User.Role.ROLE_USER);

        when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(longUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(longUsername)).thenReturn(token);

        String result = authenticationService.authenticate(longUsername, password);

        assertNotNull(result);
        assertEquals(token, result);

        verify(userRepository, times(2)).findByUsername(longUsername); // Called in authenticate and loadUserByUsername
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, times(1)).generateToken(longUsername);
    }

    @Test
    void loadUserByUsernameWithVeryLongUsername() {
        String longUsername = "a".repeat(100);
        User longUser = new User();
        longUser.setId(4L);
        longUser.setUsername(longUsername);
        longUser.setPassword(encodedPassword);
        longUser.setRole(User.Role.ROLE_USER);

        when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(longUser));

        UserDetails userDetails = authenticationService.loadUserByUsername(longUsername);

        assertNotNull(userDetails);
        assertEquals(longUsername, userDetails.getUsername());

        verify(userRepository, times(1)).findByUsername(longUsername);
    }
}