package com.api.modules.users.service;

import com.api.config.JwtConfig;
import com.api.modules.users.entity.Role;
import com.api.modules.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock JwtConfig jwtConfig;

    @InjectMocks JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        var jwtConfig2 = new JwtConfig();
        jwtConfig2.setSecret("test-secret-key-that-is-long-enough-for-hs256");
        when(jwtConfig.getSecretKey()).thenReturn(jwtConfig2.getSecretKey());

        user = User.builder()
                .email("john@example.com")
                .role(Role.PLATFORM_USER)
                .build();
    }

    // ── generateAccessToken ───────────────────────────────────────────────────

    @Test
    void generateAccessToken_returnsTokenWithCorrectEmail() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(900);

        var jwt = jwtService.generateAccessToken(user);

        assertThat(jwt).isNotNull();
        assertThat(jwt.toString()).isNotBlank();
    }

    @Test
    void generateAccessToken_tokenIsNotExpired() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(900);

        var jwt = jwtService.generateAccessToken(user);

        assertThat(jwt.isExpired()).isFalse();
    }

    @Test
    void generateAccessToken_tokenContainsUserExternalKey() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(900);

        var jwt = jwtService.generateAccessToken(user);

        assertThat(jwt.getUserExternalKey()).isEqualTo(user.getExternalKey());
    }

    // ── generateRefreshToken ──────────────────────────────────────────────────

    @Test
    void generateRefreshToken_tokenIsNotExpired() {
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(604800);

        var jwt = jwtService.generateRefreshToken(user);

        assertThat(jwt.isExpired()).isFalse();
    }

    // ── parseToken ────────────────────────────────────────────────────────────

    @Test
    void parseToken_validToken_returnsJwt() {
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(900);
        var token = jwtService.generateAccessToken(user).toString();

        var result = jwtService.parseToken(token);

        assertThat(result).isNotNull();
        assertThat(result.getUserExternalKey()).isEqualTo(user.getExternalKey());
    }

    @Test
    void parseToken_invalidToken_returnsNull() {
        var result = jwtService.parseToken("not.a.valid.token");

        assertThat(result).isNull();
    }

    @Test
    void parseToken_tokenSignedWithDifferentKey_returnsNull() {
        var otherConfig = new JwtConfig();
        otherConfig.setSecret("completely-different-secret-key-for-testing");
        var otherService = new JwtService(otherConfig);
        var tokenFromOtherService = otherService.generateAccessToken(user).toString();

        var result = jwtService.parseToken(tokenFromOtherService);

        assertThat(result).isNull();
    }
}
