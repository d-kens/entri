package com.api.modules.users.service;

import com.api.common.exception.UnauthorizedException;
import com.api.config.JwtConfig;
import com.api.modules.users.dto.AccessToken;
import com.api.modules.users.dto.CreateUserRequest;
import com.api.modules.users.dto.LoginRequest;
import com.api.modules.users.dto.LoginResponse;
import com.api.modules.users.dto.UserResponse;
import com.api.modules.users.entity.Jwt;
import com.api.modules.users.entity.User;
import com.api.modules.users.service.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock JwtConfig jwtConfig;
    @Mock UserMapper userMapper;
    @Mock JwtService jwtService;
    @Mock UserService userService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test
    void register_delegatesToUserServiceAndReturnsResult() {
        var request = new CreateUserRequest("john@example.com", "Password123!", "John", "Doe", "0712345678", "PLATFORM_USER");
        var expected = new UserResponse("PLATFORM_USER", "john@example.com", "Doe", "John", "0712345678", "ext-key");
        when(userService.create(request)).thenReturn(expected);

        var result = authService.register(request);

        assertThat(result).isEqualTo(expected);
        verify(userService).create(request);
    }

    @Test
    void login_authenticatesAndReturnsTokens() {
        var request = new LoginRequest("john@example.com", "Password123!");
        var user = User.builder().email("john@example.com").build();
        var userResponse = new UserResponse("PLATFORM_USER", "john@example.com", "Doe", "John", "0712345678", "ext-key");
        var accessJwt = mock(Jwt.class);
        var refreshJwt = mock(Jwt.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userService.findByEmail("john@example.com")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(accessJwt);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshJwt);
        when(accessJwt.toString()).thenReturn("access-token");
        when(refreshJwt.toString()).thenReturn("refresh-token");
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(900);

        var result = authService.login(request);

        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.loginResponse().accessToken().token()).isEqualTo("access-token");
        assertThat(result.loginResponse().user()).isEqualTo(userResponse);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    void refreshToken_validToken_returnsNewAccessToken() {
        var user = User.builder().email("john@example.com").build();
        var refreshJwt = mock(Jwt.class);
        var newAccessJwt = mock(Jwt.class);

        when(jwtService.parseToken("valid-refresh-token")).thenReturn(refreshJwt);
        when(refreshJwt.isExpired()).thenReturn(false);
        when(refreshJwt.getUserExternalKey()).thenReturn("ext-key");
        when(userService.findEntityByExternalKey("ext-key")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessJwt);
        when(newAccessJwt.toString()).thenReturn("new-access-token");
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(900);

        AccessToken result = authService.refreshToken("valid-refresh-token");

        assertThat(result.token()).isEqualTo("new-access-token");
        assertThat(result.expiresIn()).isEqualTo(900);
    }

    @Test
    void refreshToken_nullToken_throwsUnauthorizedException() {
        when(jwtService.parseToken("invalid-token")).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired refresh token");
    }

    @Test
    void refreshToken_expiredToken_throwsUnauthorizedException() {
        var expiredJwt = mock(Jwt.class);
        when(jwtService.parseToken("expired-token")).thenReturn(expiredJwt);
        when(expiredJwt.isExpired()).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken("expired-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired refresh token");
    }
}
