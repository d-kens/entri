package com.parrcel.api.modules.auth.service;


import com.parrcel.api.common.exception.InvalidTokenException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.auth.dto.AuthRequest;
import com.parrcel.api.modules.auth.dto.ForgotPasswordRequest;
import com.parrcel.api.modules.auth.dto.TokenPair;
import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.notification.service.NotificationService;
import com.parrcel.api.modules.token.enums.TokenPurpose;
import com.parrcel.api.modules.token.service.TokenService;
import com.parrcel.api.modules.user.entity.User;
import com.parrcel.api.modules.user.service.UserService;
import com.parrcel.api.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final NotificationService notificationService;

    public TokenPair login(AuthRequest authRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        var user = userService.getUserByEmail(authRequest.getEmail());
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new TokenPair(
                accessToken.toString(),
                refreshToken.toString()
        );
    }

    public String refreshToken(String refreshToken) {
        var refreshTokenObject = jwtService.parseToken(refreshToken);

        if (refreshTokenObject == null)
            throw new InvalidTokenException("token is invalid or has expired");

        var userId = refreshTokenObject.getUserId();

        User user;

        try {
            user = userService.getUserById(userId);
        } catch (NotFoundException exception) {
            throw new InvalidTokenException("user associated with the token not found");
        }

        var accessTokenObject = jwtService.generateAccessToken(user);
        return accessTokenObject.toString();
    }

    public String forgotPassword(ForgotPasswordRequest request) {

        try {
            var user = userService.getUserByEmail(request.getEmail());

            var tokenResponse = tokenService.generateToken(user, TokenPurpose.PASSWORD_RESET, null);

            var payload = (Map<String, Object>) (Map<?, ?>) Map.of(
                    "resetToken", tokenResponse.getRawToken(),
                    "expiresAt", tokenResponse.getExpiresAt().toString(),
                    "userName", user.getUserName()
            );
            
            notificationService.sendNotification(user, NotificationType.RESET_PASSWORD, payload);
            
            return "If email exists, a reset link has been sent";
        } catch (NotFoundException exception) {
            return "If email exists, a reset link has been sent";
        }
    }
}