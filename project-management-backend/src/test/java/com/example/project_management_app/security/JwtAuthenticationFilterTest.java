package com.example.project_management_app.security;

import com.example.project_management_app.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private Method shouldNotFilterMethod;

    @BeforeEach
    void setUp() throws Exception {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
        shouldNotFilterMethod = JwtAuthenticationFilter.class.getDeclaredMethod("shouldNotFilter", HttpServletRequest.class);
        shouldNotFilterMethod.setAccessible(true);
    }

    @Test
    void shouldNotFilterWebSocketPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/ws/chat");
        boolean result = (boolean) shouldNotFilterMethod.invoke(jwtAuthenticationFilter, request);
        assertTrue(result);
    }

    @Test
    void shouldNotFilterSockJsPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/ws/connect");
        boolean result = (boolean) shouldNotFilterMethod.invoke(jwtAuthenticationFilter, request);
        assertTrue(result);
    }

    @Test
    void shouldFilterApiEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/me");
        boolean result = (boolean) shouldNotFilterMethod.invoke(jwtAuthenticationFilter, request);
        assertFalse(result);
    }

    @Test
    void shouldFilterRootPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/");
        boolean result = (boolean) shouldNotFilterMethod.invoke(jwtAuthenticationFilter, request);
        assertFalse(result);
    }
}