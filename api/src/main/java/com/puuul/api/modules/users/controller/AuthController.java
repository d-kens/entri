package com.puuul.api.modules.users.controller;

import com.puuul.api.config.JwtConfig;
import com.puuul.api.modules.users.dto.*;
import com.puuul.api.modules.users.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateUserDto userDto
    ) {
        var response = authService.register(userDto);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(response.externalKey()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/login")
    public LoginResponseDto login(
            HttpServletResponse response,
            @Valid @RequestBody LoginRequestDto loginRequest
    ) {
        var result = authService.login(loginRequest);
        setRefreshTokenCookie(
                response,
                result.refreshToken(),
                jwtConfig.getRefreshTokenExpiration()
        );
        return result.loginResponseDto();
    }

    @PostMapping("refresh-token")
    public AccessTokenDto refreshToken(
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
