package com.entri.auth.service;

import com.entri.auth.dto.AccessToken;
import com.entri.auth.dto.LoginRequest;
import com.entri.auth.dto.LoginResponse;
import com.entri.auth.dto.LoginResult;
import com.entri.auth.dto.RefreshResult;
import com.entri.security.JwtService;
import com.entri.security.JwtConfig;
import com.entri.users.dto.CreateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.mapper.UserMapper;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public UserResponse register(CreateUserRequest userDto) {
        return userService.create(userDto);
    }

    public LoginResult login(LoginRequest loginRequest) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.email(), loginRequest.password()
        );
        authenticationManager.authenticate(authRequest);
        var user = userService.findByEmail(loginRequest.email());
        String accessToken = jwtService.generateAccessToken(user).toString();
        String refreshToken = refreshTokenService.issue(user);
        var loginResponse = new LoginResponse(
                userMapper.toResponse(user),
                new AccessToken(accessToken, jwtConfig.getAccessTokenExpiration())
        );
        return new LoginResult(loginResponse, refreshToken);
    }

    public RefreshResult refreshToken(String refreshToken) {
        var rotation = refreshTokenService.rotate(refreshToken);
        String accessToken = jwtService.generateAccessToken(rotation.user()).toString();
        int maxAge = (int) Math.max(0, Duration.between(Instant.now(), rotation.expiresAt()).toSeconds());
        return new RefreshResult(
                new AccessToken(accessToken, jwtConfig.getAccessTokenExpiration()),
                rotation.refreshToken(),
                maxAge
        );
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}
