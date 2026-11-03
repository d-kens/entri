package com.entri.auth.service;

import com.entri.auth.entity.RefreshToken;
import com.entri.auth.repository.RefreshTokenRepository;
import com.entri.exception.UnauthorizedException;
import com.entri.security.JwtConfig;
import com.entri.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Opaque, rotating refresh tokens. The raw token only lives in the user's httpOnly cookie; the database stores its hash.
 * <p>
 * Each refresh replaces the token with a new one that keeps the original expiry, so a session ends a fixed time
 * after login. Presenting an already-rotated token after {@link #REUSE_GRACE} means it was copied, so every
 * session of that user is revoked. Inside the grace window it is accepted, so parallel tabs refreshing at the same
 * moment don't log each other out.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    static final Duration REUSE_GRACE = Duration.ofSeconds(10);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    public record Rotation(User user, String refreshToken, Instant expiresAt) {}

    @Transactional
    public String issue(User user) {
        var now = Instant.now();
        return save(user, now, now.plus(Duration.ofSeconds(jwtConfig.getRefreshTokenExpiration())));
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public Rotation rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalid();
        }
        var now = Instant.now();
        var token = refreshTokenRepository.findByTokenHashForUpdate(OpaqueTokens.hash(rawToken))
                .orElseThrow(RefreshTokenService::invalid);
        var user = token.getUser();

        if (token.getRevokedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw invalid();
        }

        if (token.getRotatedAt() != null && token.getRotatedAt().plus(REUSE_GRACE).isBefore(now)) {
            log.warn("Rotated refresh token reused for user {}; revoking all of their sessions", user.getExternalKey());
            refreshTokenRepository.revokeAllForUser(user, now);
            throw invalid();
        }

        if (!user.isEnabled() || user.isDeleted()) {
            throw invalid();
        }

        if (token.getRotatedAt() == null) {
            token.setRotatedAt(now);
        }
        String newToken = save(user, now, token.getExpiresAt());
        return new Rotation(user, newToken, token.getExpiresAt());
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            refreshTokenRepository.revokeByTokenHash(OpaqueTokens.hash(rawToken), Instant.now());
        }
    }

    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllForUser(user, Instant.now());
    }

    private String save(User user, Instant now, Instant expiresAt) {
        String rawToken = OpaqueTokens.generate();
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(OpaqueTokens.hash(rawToken))
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build());
        return rawToken;
    }

    private static UnauthorizedException invalid() {
        return new UnauthorizedException("Invalid or expired refresh token");
    }
}
