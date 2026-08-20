package com.entri.users.service;


import com.entri.shared.exception.ResourceNotFoundException;
import com.entri.shared.exception.UnauthorizedException;
import com.entri.users.dto.ResetPasswordRequest;
import com.entri.users.entity.PasswordResetToken;
import com.entri.users.entity.User;
import com.entri.integrations.novu.WorkflowType;
import com.entri.notification.event.NotificationEvent;
import com.entri.users.repository.PasswordResetTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
@Transactional()
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.password-reset.expiry-minutes}")
    private long expiryMinutes;

    private static final SecureRandom secureRandom = new SecureRandom();

    public void forgotPassword(final String email) {
        User user;
        try {
            user = userService.findByEmail(email);
        } catch (ResourceNotFoundException e) {
            return;
        }
        String rawToken = generate();
        String tokenHash = hash(rawToken);
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                .build();
        passwordResetTokenRepository.save(token);

        String resetUrl = appBaseUrl + "auth/reset-password?token=" + rawToken;
        eventPublisher.publishEvent(new NotificationEvent(
                WorkflowType.PASSWORD_RESET,
                user.getExternalKey().toString(),
                Map.of(
                        "resetUrl", resetUrl,
                        "firstName", user.getFirstName(),
                        "expiryMinutes", expiryMinutes
                )
        ));
    }

    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hash(request.token());
        PasswordResetToken token =
                passwordResetTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new UnauthorizedException("Invalid password reset token"));
        if (!token.isValid()) {
            throw new UnauthorizedException("Password reset token expired or already used");
        }
        var user = token.getUser();
        userService.changeUserPassword(user, request.password());
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        eventPublisher.publishEvent(new NotificationEvent(
                WorkflowType.UPDATED_PASSWORD,
                user.getExternalKey().toString(),
                Map.of(
                        "firstName", user.getFirstName()
                )
        ));
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashed = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder sb = new StringBuilder();

            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
