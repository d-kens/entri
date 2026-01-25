package com.parrcel.api.modules.auth.controller;

import com.parrcel.api.modules.auth.dto.*;
import com.parrcel.api.modules.auth.service.AuthService;
import com.parrcel.api.security.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtConfig jwtConfig;
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(
            HttpServletResponse response,
            @Valid @RequestBody AuthRequest authRequest
    ) {
        var tokenPair = authService.login(authRequest);

        var cookie = new Cookie("refreshToken", tokenPair.getRefreshToken());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh-token");
        cookie.setMaxAge(jwtConfig.getAccessTokenExpiration());

        response.addCookie(cookie);

        return new AuthResponse(tokenPair.getAccessToken());
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