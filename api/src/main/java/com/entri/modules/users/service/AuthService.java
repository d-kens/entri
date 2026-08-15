package com.entri.modules.users.service;


import com.entri.common.exception.UnauthorizedException;
import com.entri.infrastructure.config.JwtConfig;
import com.entri.modules.users.dto.AccessToken;
import com.entri.modules.users.dto.CreateUserRequest;
import com.entri.modules.users.dto.LoginRequest;
import com.entri.modules.users.dto.LoginResponse;
import com.entri.modules.users.dto.LoginResult;
import com.entri.modules.users.dto.UserResponse;
import com.entri.modules.users.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(CreateUserRequest userDto) {
        return userService.create(userDto);
    }

    public LoginResult login(LoginRequest loginRequest) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.email(), loginRequest.password()
        );
        Authentication authResponse = authenticationManager.authenticate(authRequest);
        var user = userService.findByEmail(loginRequest.email());
        String accessToken = jwtService.generateAccessToken(user).toString();
        String refreshToken = jwtService.generateRefreshToken(user).toString();
        var loginResponse = new LoginResponse(
                userMapper.toResponse(user),
                new AccessToken(accessToken, jwtConfig.getAccessTokenExpiration())
        );
        return new LoginResult(loginResponse, refreshToken);
    }

    public AccessToken refreshToken(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        var user = userService.findEntityByExternalKey(jwt.getUserExternalKey());
        String accessToken = jwtService.generateAccessToken(user).toString();
        return new AccessToken(accessToken, jwtConfig.getAccessTokenExpiration());
    }
}
