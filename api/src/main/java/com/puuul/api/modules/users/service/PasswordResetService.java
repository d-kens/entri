package com.puuul.api.modules.users.service;


import com.puuul.api.common.exception.NotFoundException;
import com.puuul.api.modules.users.entity.PasswordResetToken;
import com.puuul.api.modules.users.entity.User;
import com.puuul.api.client.novu.WorkflowType;
import com.puuul.api.modules.notification.event.NotificationEvent;
import com.puuul.api.modules.users.repository.PasswordResetTokenRepository;
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
        } catch (NotFoundException e) {
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

        String resetUrl = appBaseUrl + "/reset-password?token=" + rawToken;
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
