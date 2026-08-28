package com.entri.auth.controller;

import com.entri.auth.dto.AccessToken;
import com.entri.auth.dto.ForgotPasswordRequest;
import com.entri.auth.dto.LoginRequest;
import com.entri.auth.dto.LoginResponse;
import com.entri.auth.dto.ResetPasswordRequest;
import com.entri.auth.service.AuthService;
import com.entri.auth.service.PasswordResetService;
import com.entri.security.JwtConfig;
import com.entri.users.dto.CreateUserRequest;
import com.entri.users.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final JwtConfig jwtConfig;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    public ResponseEntity<UserResponse> register(
            UriComponentsBuilder uriComponentsBuilder,
            CreateUserRequest userDto
    ) {
        var response = authService.register(userDto);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(response.externalKey()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public LoginResponse login(
            HttpServletResponse response,
            LoginRequest loginRequest
    ) {
        var result = authService.login(loginRequest);
        setRefreshTokenCookie(response, result.refreshToken(), jwtConfig.getRefreshTokenExpiration());
        return result.loginResponse();
    }

    @Override
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "A reset link has successfully been sent to your email if it was found in our system"));
    }

    @Override
    public ResponseEntity<Void> resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    public AccessToken refreshToken(
            HttpServletResponse response,
            String refreshToken
    ) {
        return authService.refreshToken(refreshToken);
    }

    @Override
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
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
