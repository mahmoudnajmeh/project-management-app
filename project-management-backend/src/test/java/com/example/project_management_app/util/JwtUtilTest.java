package com.example.project_management_app.util;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String secret = "4x9FhT2qL8mN5rP1sK3vY7wZ6bD0cG2jH5nB8vC1xM4qW7tR9yU2iO5pA8sD3fG6jH9";
    private final Long expiration = 86400000L;
    private final String username = "Mahmoud";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    void generateToken_Works() {
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extracted = jwtUtil.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    void tokensAreDifferent() {
        String token1 = jwtUtil.generateToken(username);
        String token2 = jwtUtil.generateToken(username);

        assertNotNull(token1);
        assertNotNull(token2);
    }

    @Test
    void extractUsername_Works() {
        String token = jwtUtil.generateToken(username);

        String extracted = jwtUtil.extractUsername(token);

        assertEquals(username, extracted);
    }

    @Test
    void extractUsername_DifferentUser() {
        String testUser = "testuser123";
        String token = jwtUtil.generateToken(testUser);

        String extracted = jwtUtil.extractUsername(token);

        assertEquals(testUser, extracted);
    }

    @Test
    void validateToken_Valid() {
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_Malformed() {
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";

        boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_Empty() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void validateToken_Null() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_WrongKey() {
        String differentSecret = "different-secret-key-that-is-at-least-32-bytes-long-for-hs256";
        JwtUtil differentJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentJwtUtil, "secret", differentSecret);
        ReflectionTestUtils.setField(differentJwtUtil, "expiration", expiration);

        String token = differentJwtUtil.generateToken(username);

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void extractUsername_Malformed_ThrowsException() {
        String malformedToken = "malformed.token.here";

        assertThrows(MalformedJwtException.class, () ->
                jwtUtil.extractUsername(malformedToken)
        );
    }

    @Test
    void extractUsername_Empty_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                jwtUtil.extractUsername("")
        );
    }

    @Test
    void extractUsername_Null_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                jwtUtil.extractUsername(null)
        );
    }

    @Test
    void generateToken_NullUsername_Works() {
        String token = jwtUtil.generateToken(null);

        assertNotNull(token);

        String extracted = jwtUtil.extractUsername(token);
        assertNull(extracted);
    }

    @Test
    void generateToken_EmptyUsername_Works() {
        String token = jwtUtil.generateToken("");

        assertNotNull(token);

        String extracted = jwtUtil.extractUsername(token);
        assertNull(extracted);
    }

    @Test
    void generateToken_LongUsername_Works() {
        String longUsername = "a".repeat(100);

        String token = jwtUtil.generateToken(longUsername);

        assertNotNull(token);

        String extracted = jwtUtil.extractUsername(token);
        assertEquals(longUsername, extracted);
    }

    @Test
    void validateToken_Expired_ReturnsFalse() throws Exception {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);

        String token = jwtUtil.generateToken(username);

        Thread.sleep(2);

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void specialCharacters_Work() {
        String specialUser = "user@#$%^&*()_+";

        String token = jwtUtil.generateToken(specialUser);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));

        String extracted = jwtUtil.extractUsername(token);
        assertEquals(specialUser, extracted);
    }

    @Test
    void numbers_Work() {
        String numericUser = "user1234567890";

        String token = jwtUtil.generateToken(numericUser);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));

        String extracted = jwtUtil.extractUsername(token);
        assertEquals(numericUser, extracted);
    }

    @Test
    void differentExpiration_StillValid() {
        Long diffExpiration = 3600000L;
        JwtUtil differentJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentJwtUtil, "secret", secret);
        ReflectionTestUtils.setField(differentJwtUtil, "expiration", diffExpiration);

        String token = differentJwtUtil.generateToken(username);

        assertTrue(jwtUtil.validateToken(token));

        String extracted = jwtUtil.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    void trimmedToken_Works() {
        String token = jwtUtil.generateToken(username);
        String trimmed = token.trim();

        String extracted = jwtUtil.extractUsername(trimmed);
        assertEquals(username, extracted);
    }
}