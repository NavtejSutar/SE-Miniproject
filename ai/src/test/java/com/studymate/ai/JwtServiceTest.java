package com.studymate.ai;

import com.studymate.ai.Security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 256-bit test secret (base64 encoded)
        ReflectionTestUtils.setField(jwtService, "secretKey", "dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIGp3dA==");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void testGenerateAndExtractUsername() {
        UserDetails userDetails = new User("student@example.com", "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String extractedEmail = jwtService.extractUsername(token);
        assertEquals("student@example.com", extractedEmail);
    }

    @Test
    void testIsTokenValid() {
        UserDetails userDetails = new User("student@example.com", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));

        UserDetails otherUser = new User("other@example.com", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}
