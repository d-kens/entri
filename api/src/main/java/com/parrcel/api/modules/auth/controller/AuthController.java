package com.parrcel.api.modules.auth.controller;

import com.parrcel.api.modules.auth.dto.*;
import com.parrcel.api.modules.auth.service.AuthService;
import com.parrcel.api.modules.user.dto.UserResponse;
import com.parrcel.api.modules.user.mapper.UserMapper;
import com.parrcel.api.modules.user.service.UserService;
import com.parrcel.api.security.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private UserMapper userMapper;
    private UserService userService;
    private final JwtConfig jwtConfig;
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(
            HttpServletResponse response,
            @Valid @RequestBody AuthRequest authRequest
    ) {
        var tokenPair = authService.login(authRequest);

        var cookie = new Cookie("refreshToken", tokenPair.getRefreshToken());
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh-token");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());


        response.addCookie(cookie);

        return new AuthResponse(tokenPair.getAccessToken());
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @AuthenticationPrincipal Long userId
    ) {
        var user = userService.getUserById(userId);

        return userMapper.toResponse(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response
    ) {
        var cookie = new Cookie("refreshToken", null);
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh-token");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue("refreshToken") String refreshToken
    ) {
        var accessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok().body(new AuthResponse(accessToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        var response = authService.forgotPassword(request);
        return ResponseEntity.ok(
                Map.of("message", response)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        var response = authService.resetPassword(request);
        return ResponseEntity.ok(
                Map.of("message", response)
        );
    }
}