package com.example.project_management_app.security;

import com.example.project_management_app.config.JwtAuthenticationFilter;
import com.example.project_management_app.config.OAuth2AuthenticationSuccessHandler;
import com.example.project_management_app.config.SecurityConfig;
import com.example.project_management_app.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();

        setField(securityConfig, "jwtUtil", jwtUtil);
        setField(securityConfig, "oAuth2AuthenticationSuccessHandler", oAuth2AuthenticationSuccessHandler);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void passwordEncoder_Works() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);

        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void authManager_Works() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(manager);
        assertEquals(mockManager, manager);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    @Test
    void jwtFilter_CreatesInstance() {
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter();

        assertNotNull(filter);
    }

    @Test
    void corsSource_ConfiguredCorrectly() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();

        assertNotNull(source);

        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertEquals(Arrays.asList("http://localhost:3000"), config.getAllowedOrigins());
        assertEquals(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"), config.getAllowedMethods());
        assertEquals(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"), config.getAllowedHeaders());
        assertEquals(Arrays.asList("Authorization"), config.getExposedHeaders());
        assertTrue(config.getAllowCredentials());
        assertEquals(3600L, config.getMaxAge().longValue());
    }

    @Test
    void cors_AllowsLocalhost() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
        assertEquals(1, config.getAllowedOrigins().size());
        assertFalse(config.getAllowedOrigins().contains("*"));
    }

    @Test
    void cors_AllowsHttpMethods() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertEquals(6, config.getAllowedMethods().size());
        assertTrue(config.getAllowedMethods().containsAll(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        ));
    }

    @Test
    void cors_AllowsHeaders() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertEquals(4, config.getAllowedHeaders().size());
        assertTrue(config.getAllowedHeaders().contains("Authorization"));
        assertTrue(config.getAllowedHeaders().contains("Content-Type"));
        assertTrue(config.getAllowedHeaders().contains("Accept"));
        assertTrue(config.getAllowedHeaders().contains("X-Requested-With"));

        assertEquals(1, config.getExposedHeaders().size());
        assertTrue(config.getExposedHeaders().contains("Authorization"));
    }

    @Test
    void cors_AllowsCredentials() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertTrue(config.getAllowCredentials());
    }

    @Test
    void cors_HasMaxAge() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertEquals(3600L, config.getMaxAge().longValue());
    }

    @Test
    void beans_Exist() {
        assertNotNull(securityConfig.passwordEncoder());
        assertNotNull(securityConfig.jwtAuthenticationFilter());
        assertNotNull(securityConfig.corsConfigurationSource());
    }
}