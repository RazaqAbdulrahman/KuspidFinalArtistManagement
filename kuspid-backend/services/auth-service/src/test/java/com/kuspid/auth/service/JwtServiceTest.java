package com.kuspid.auth.service;

import com.kuspid.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set private fields using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "e83d8c7c9f2b1a0e9d8c7b6a5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        testUser = User.builder()
                .id(1L)
                .email("test@kuspid.com")
                .password("password")
                .fullName("Test User")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("Generate Token - Should create valid JWT")
    void generateToken_ShouldCreateValidJwt() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Extract Username - Should return email from token")
    void extractUsername_ShouldReturnEmail() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@kuspid.com");
    }

    @Test
    @DisplayName("Validate Token - Should return true for valid token")
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Generate Refresh Token - Should create longer-lived token")
    void generateRefreshToken_ShouldCreateToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);

        assertThat(refreshToken).isNotNull();
        assertThat(jwtService.extractUsername(refreshToken)).isEqualTo("test@kuspid.com");
    }
}
