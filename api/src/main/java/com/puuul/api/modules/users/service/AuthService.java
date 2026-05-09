package com.puuul.api.modules.users.service;


import com.puuul.api.common.exception.UnauthorizedException;
import com.puuul.api.config.JwtConfig;
import com.puuul.api.modules.users.dto.*;
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

    public LoginResultDto login(LoginRequestDto loginRequest) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.email(), loginRequest.password()
        );
        Authentication authResponse = authenticationManager.authenticate(authRequest);
        var user = userService.findByEmail(loginRequest.email());
        String accessToken = jwtService.generateAccessToken(user).toString();
        String refreshToken = jwtService.generateRefreshToken(user).toString();
        var loginResponse = new LoginResponseDto(
                userService.toResponse(user),
                new AccessTokenDto(accessToken, jwtConfig.getAccessTokenExpiration())
        );
        return new LoginResultDto(loginResponse, refreshToken);
    }

    public AccessTokenDto refreshToken(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        var user = userService.findEntityByExternalKey(jwt.getUserExternalKey());
        String accessToken = jwtService.generateAccessToken(user).toString();
        return new AccessTokenDto(accessToken, jwtConfig.getAccessTokenExpiration());
    }
}
