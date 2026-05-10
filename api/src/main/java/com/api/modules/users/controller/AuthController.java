package com.api.modules.users.controller;

import com.api.config.JwtConfig;
import com.api.modules.users.dto.*;
import com.api.modules.users.service.AuthService;
import com.api.modules.users.service.PasswordResetService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtConfig jwtConfig;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateUserRequest userDto
    ) {
        var response = authService.register(userDto);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(response.externalKey()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/login")
    public LoginResponse login(
            HttpServletResponse response,
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        var result = authService.login(loginRequest);
        setRefreshTokenCookie(
                response,
                result.refreshToken(),
                jwtConfig.getRefreshTokenExpiration()
        );
        return result.loginResponse();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "A reset link has successfully been sent to your email if it was found in our system"));
    }

    @PostMapping("refresh-token")
    public AccessToken refreshToken(
            HttpServletResponse response,
            @CookieValue(value = "refreshToken") String refreshToken
    ) {
        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("refresh_token"))
                    .findFirst()
                    .ifPresent(c -> setRefreshTokenCookie(response, "", 0));
        }
        return ResponseEntity.noContent().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String value, int maxAge) {
        Cookie cookie = new Cookie("refresh_token", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
