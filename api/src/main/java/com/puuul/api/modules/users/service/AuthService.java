package com.puuul.api.modules.users.service;


import com.puuul.api.config.JwtConfig;
import com.puuul.api.modules.users.dto.CreateUserDto;
import com.puuul.api.modules.users.dto.LoginRequestDto;
import com.puuul.api.modules.users.dto.LoginResponseDto;
import com.puuul.api.modules.users.dto.UserResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserResponseDto register(CreateUserDto userDto) {
        return userService.create(userDto);
    }

    public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletResponse response) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.email(), loginRequest.password()
        );
        Authentication authResponse = authenticationManager.authenticate(authRequest);

        var principal = (org.springframework.security.core.userdetails.UserDetails) authResponse.getPrincipal();
        var user = userService.findByEmail(principal.getUsername());

        String accessToken = jwtService.generateAccessToken(user).toString();
        String refreshToken = jwtService.generateRefreshToken(user).toString();
        setRefreshTokenCookie(response, refreshToken);

        return new LoginResponseDto(
                new LoginResponseDto.UserInfo(
                        user.getRole().toString(),
                        user.getEmail(),
                        user.getExternalKey().toString()
                ),
                jwtConfig.getAccessTokenExpiration(),
                accessToken
        );
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        response.addCookie(cookie);
    }
}
