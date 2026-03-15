package com.parrcel.api.modules.auth.service;

import com.parrcel.api.common.exception.InvalidTokenException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.auth.dto.*;
import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.notification.events.SendNotificationEvent;
import com.parrcel.api.modules.token.config.TokenConfig;
import com.parrcel.api.modules.token.enums.TokenPurpose;
import com.parrcel.api.modules.token.model.Token;
import com.parrcel.api.modules.token.model.RefreshTokenSession;
import com.parrcel.api.modules.token.repository.RefreshTokenSessionRepository;
import com.parrcel.api.modules.token.service.TokenService;
import com.parrcel.api.modules.users.model.User;
import com.parrcel.api.modules.users.service.UserService;
import com.parrcel.api.security.config.JwtConfig;
import com.parrcel.api.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.base-url}")
    private String baseUrl;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenConfig tokenConfig;
    private final JwtConfig jwtConfig;
    private final TokenService tokenService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;

    public TokenPair login(AuthRequest authRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.phoneNumber(),
                        authRequest.password()
                )
        );

        var user = userService.getUserByPhoneNumber(authRequest.phoneNumber());
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        createRefreshTokenSession(user, refreshToken.toString());

        return new TokenPair(
                accessToken.toString(),
                refreshToken.toString()
        );
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException("refresh token is required for logout");
        }

        var tokenHash = hashToken(refreshToken);
        var session = refreshTokenSessionRepository.findByRefreshTokenHashAndIsRevokedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("refresh token session not found or already revoked"));

        session.setIsRevoked(true);
        refreshTokenSessionRepository.save(session);
    }

    public String refreshToken(String refreshToken) {
        var refreshTokenObject = jwtService.parseToken(refreshToken);

        if (refreshTokenObject == null)
            throw new InvalidTokenException("token is invalid or has expired");

        var tokenHash = hashToken(refreshToken);
        var session = refreshTokenSessionRepository.findByRefreshTokenHashAndIsRevokedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("refresh token session not found or has been revoked"));

        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new InvalidTokenException("refresh token has expired");
        }

        var userId = refreshTokenObject.getUserId();

        User user;

        try {
            user = userService.getUserById(userId);
        } catch (NotFoundException exception) {
            throw new InvalidTokenException("user associated with the token not found");
        }

        refreshTokenSessionRepository.save(session);

        var accessTokenObject = jwtService.generateAccessToken(user);
        return accessTokenObject.toString();
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        try {
            var user = userService.getUserByPhoneNumber(request.phoneNumber());

            var tokenResponse = tokenService.generateToken(user, TokenPurpose.PASSWORD_RESET, null);

            String resetUrl = String.format("%s/auth/reset-password?token=%s", baseUrl, tokenResponse.rawToken());

            Map<String, Object> payload = Map.of(
                    "resetUrl", resetUrl,
                    "expiresIn", String.valueOf(tokenConfig.getEmailVerificationExpirationSeconds() / 60) + " minutes",
                    "userName", user.getUserName()
            );

            eventPublisher.publishEvent(
                    new SendNotificationEvent(user, payload, NotificationType.RESET_PASSWORD)
            );

            return "If phone number exists, a reset link has been sent";
        } catch (NotFoundException exception) {
            return "If phone number exists, a reset link has been sent";
        }
    }


    public String resetPassword(ResetPasswordRequest request) {
        Token token = tokenService.validateToken(request.token());

        if (token.getPurpose() != TokenPurpose.PASSWORD_RESET) {
            throw new InvalidTokenException("Token is not valid for password reset");
        }

        User user = token.getUser();
        userService.updatePassword(user, request.newPassword());
        tokenService.invalidateToken(token);

        var loginUrl = String.format("%s/auth/login", baseUrl);

        Map<String, Object> payload = Map.of(
                "loginUrl", loginUrl,
                "userName", user.getUserName()
        );

        eventPublisher.publishEvent(
                new SendNotificationEvent(user, payload, NotificationType.PASSWORD_RESET_CONFIRMATION)
        );

        return "Password has been successfully reset";
    }

    private void createRefreshTokenSession(User user, String refreshToken) {
        var session = new RefreshTokenSession();
        session.setUser(user);
        session.setRefreshTokenHash(hashToken(refreshToken));
        session.setIssuedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration()));
        session.setIsRevoked(false);

        refreshTokenSessionRepository.save(session);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}