package com.entri.modules.service;

import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.notification.NotificationEventPublisher;
import com.entri.notification.dto.NotificationMessage;
import com.entri.auth.dto.ResetPasswordRequest;
import com.entri.auth.entity.PasswordResetToken;
import com.entri.users.entity.User;
import com.entri.auth.repository.PasswordResetTokenRepository;
import com.entri.auth.service.PasswordResetService;
import com.entri.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock UserService userService;
    @Mock NotificationEventPublisher notificationPublisher;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks PasswordResetService passwordResetService;

    private final User user = User.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

    private void injectFields() {
        ReflectionTestUtils.setField(passwordResetService, "appBaseUrl", "https://oro.co.ke/");
        ReflectionTestUtils.setField(passwordResetService, "expiryMinutes", 15L);
    }

    // ── forgotPassword ────────────────────────────────────────────────────────

    @Test
    void forgotPassword_knownEmail_savesTokenAndPublishesEvent() {
        injectFields();
        when(userService.findByEmail("john@example.com")).thenReturn(user);

        passwordResetService.forgotPassword("john@example.com");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(notificationPublisher).publish(any(NotificationMessage.class));
    }

    @Test
    void forgotPassword_unknownEmail_doesNothingAndDoesNotRevealUserExistence() {
        injectFields();
        when(userService.findByEmail("unknown@example.com")).thenThrow(new ResourceNotFoundException("User not found"));

        passwordResetService.forgotPassword("unknown@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(notificationPublisher, never()).publish(any());
    }

    @Test
    void forgotPassword_savedToken_isNotExpired() {
        injectFields();
        when(userService.findByEmail("john@example.com")).thenReturn(user);
        var tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        passwordResetService.forgotPassword("john@example.com");

        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isExpired()).isFalse();
        assertThat(tokenCaptor.getValue().isValid()).isTrue();
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_validToken_changesPasswordAndMarksTokenUsed() {
        injectFields();
        var token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashOf("valid-raw-token"))
                .expiresAt(Instant.now().plusSeconds(900))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(hashOf("valid-raw-token")))
                .thenReturn(Optional.of(token));

        passwordResetService.resetPassword(new ResetPasswordRequest("valid-raw-token", "NewPassword123!"));

        verify(userService).changeUserPassword(user, "NewPassword123!");
        assertThat(token.isUsed()).isTrue();
        verify(passwordResetTokenRepository).save(token);
        verify(notificationPublisher).publish(any(NotificationMessage.class));
    }

    @Test
    void resetPassword_invalidToken_throwsUnauthorizedException() {
        injectFields();
        when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword(new ResetPasswordRequest("bad-token", "NewPassword123!")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid password reset token");
    }

    @Test
    void resetPassword_expiredToken_throwsUnauthorizedException() {
        injectFields();
        var expiredToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashOf("expired-token"))
                .expiresAt(Instant.now().minusSeconds(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(hashOf("expired-token")))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> passwordResetService.resetPassword(new ResetPasswordRequest("expired-token", "NewPassword123!")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Password reset token expired or already used");
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsUnauthorizedException() {
        injectFields();
        var usedToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashOf("used-token"))
                .expiresAt(Instant.now().plusSeconds(900))
                .used(true)
                .build();
        when(passwordResetTokenRepository.findByTokenHash(hashOf("used-token")))
                .thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> passwordResetService.resetPassword(new ResetPasswordRequest("used-token", "NewPassword123!")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Password reset token expired or already used");
    }

    private String hashOf(String input) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var hashed = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
