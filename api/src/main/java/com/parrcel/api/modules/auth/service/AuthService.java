package com.parrcel.api.modules.auth.service;

import com.parrcel.api.common.exception.InvalidTokenException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.auth.dto.AuthRequest;
import com.parrcel.api.modules.auth.dto.ForgotPasswordRequest;
import com.parrcel.api.modules.auth.dto.ResetPasswordRequest;
import com.parrcel.api.modules.auth.dto.TokenPair;
import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.notification.service.NotificationService;
import com.parrcel.api.modules.token.config.TokenConfig;
import com.parrcel.api.modules.token.entity.Token;
import com.parrcel.api.modules.token.enums.TokenPurpose;
import com.parrcel.api.modules.token.service.TokenService;
import com.parrcel.api.modules.user.entity.User;
import com.parrcel.api.modules.user.service.UserService;
import com.parrcel.api.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.base-url}")
    private String baseUrl;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenConfig tokenConfig;
    private final TokenService tokenService;
    private final NotificationService notificationService;
    private final AuthenticationManager authenticationManager;

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

            String resetUrl = buildResetPasswordUrl(tokenResponse.getRawToken());

            Map<String, Object> payload = Map.of(
                    "resetUrl", resetUrl,
                    "expiresIn", String.valueOf(tokenConfig.getEmailVerificationExpirationSeconds() / 60) + " minutes",
                    "userName", user.getUserName()
            );

            notificationService.sendNotification(user, NotificationType.RESET_PASSWORD, payload);

            return "If email exists, a reset link has been sent";
        } catch (NotFoundException exception) {
            System.out.println("User not found");
            return "If email exists, a reset link has been sent";
        }
    }


    public String resetPassword(ResetPasswordRequest request) {
        Token token = tokenService.validateToken(request.getToken());

        System.out.println("================================================================");
        System.out.println(token);
        System.out.println("================================================================");

        if (token.getPurpose() != TokenPurpose.PASSWORD_RESET) {
            throw new InvalidTokenException("Token is not valid for password reset");
        }

        User user = token.getUser();
        userService.updatePassword(user, request.getNewPassword());
        tokenService.invalidateToken(token);

        return "Password has been successfully reset";
    }

    private String buildResetPasswordUrl(String token) {
        return String.format("%s/auth/reset-password?token=%s", baseUrl, token);
    }
}