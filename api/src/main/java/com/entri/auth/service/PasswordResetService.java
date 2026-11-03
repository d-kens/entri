package com.entri.auth.service;

import com.entri.auth.dto.ResetPasswordRequest;
import com.entri.auth.entity.PasswordResetToken;
import com.entri.auth.repository.PasswordResetTokenRepository;
import com.entri.notification.NotificationEventPublisher;
import com.entri.notification.NotificationType;
import com.entri.notification.dto.NotificationEvent;
import com.entri.notification.dto.NotificationRecipient;
import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.users.entity.User;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserService userService;
    private final NotificationEventPublisher notificationPublisher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.password-reset.expiry-minutes}")
    private long expiryMinutes;

    @Value("${app.password-reset.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

    public void forgotPassword(final String email) {
        User user;
        try {
            user = userService.findByEmail(email);
        } catch (ResourceNotFoundException e) {
            return;
        }

        // A per-IP rate limit at the proxy can be bypassed with a proxy pool; this
        // per-account check can't. There's no createdAt column — derive it from
        // expiresAt (createdAt = expiresAt - expiryMinutes) rather than adding one.
        // A short cooldown, not the full token TTL: a legitimate user whose email
        // is delayed/filtered should be able to ask again well before 15 minutes.
        var latestUnused = passwordResetTokenRepository.findFirstByUserAndUsedFalseOrderByExpiresAtDesc(user);
        if (latestUnused.isPresent()) {
            Instant lastIssuedAt = latestUnused.get().getExpiresAt().minus(Duration.ofMinutes(expiryMinutes));
            if (Instant.now().isBefore(lastIssuedAt.plus(Duration.ofSeconds(resendCooldownSeconds)))) {
                return;
            }
        }

        String rawToken = OpaqueTokens.generate();
        String tokenHash = OpaqueTokens.hash(rawToken);
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                .build();
        passwordResetTokenRepository.save(token);

        String resetUrl = appBaseUrl.stripTrailing() + "/auth/reset-password?token=" + rawToken;
        notificationPublisher.publish(new NotificationEvent(
                NotificationType.PASSWORD_RESET,
                user.getExternalKey().toString(),
                Map.of(
                        "resetUrl", resetUrl,
                        "firstName", user.getFirstName(),
                        "expiryMinutes", expiryMinutes
                ),
                new NotificationRecipient(user.getExternalKey().toString(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber())
        ));
    }

    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = OpaqueTokens.hash(request.token());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid password reset token"));
        if (!token.isValid()) {
            throw new UnauthorizedException("Password reset token expired or already used");
        }
        var user = token.getUser();
        userService.changeUserPassword(user, request.password());
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        notificationPublisher.publish(new NotificationEvent(
                NotificationType.UPDATED_PASSWORD,
                user.getExternalKey().toString(),
                Map.of("firstName", user.getFirstName()),
                new NotificationRecipient(user.getExternalKey().toString(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber())
        ));
    }

}
