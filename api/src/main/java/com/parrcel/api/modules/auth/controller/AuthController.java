package com.parrcel.api.modules.auth.controller;

import com.parrcel.api.modules.auth.dto.AccessToken;
import com.parrcel.api.modules.auth.dto.AuthRequest;
import com.parrcel.api.modules.auth.service.AuthService;
import com.parrcel.api.security.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtConfig jwtConfig;
    private final AuthService authService;

    @PostMapping("/login")
    public AccessToken login(
            HttpServletResponse response,
            @Valid @RequestBody AuthRequest authRequest
    ) {
        var authResponse = authService.login(authRequest);

        var cookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh-token");
        cookie.setMaxAge(jwtConfig.getAccessTokenExpiration());

        response.addCookie(cookie);

        return new AccessToken(authResponse.getAccessToken());
    }
}