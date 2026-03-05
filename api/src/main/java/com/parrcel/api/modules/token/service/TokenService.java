package com.parrcel.api.modules.token.service;

import com.parrcel.api.common.exception.InvalidTokenException;
import com.parrcel.api.modules.token.config.TokenConfig;
import com.parrcel.api.modules.token.dto.TokenResponseDto;
import com.parrcel.api.modules.token.model.Token;
import com.parrcel.api.modules.token.enums.TokenPurpose;
import com.parrcel.api.modules.token.repository.TokenRepository;
import com.parrcel.api.modules.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final TokenConfig tokenConfig;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public TokenResponseDto generateToken(User user, TokenPurpose purpose, String requestIp) {
        tokenRepository.invalidateUnusedTokensByUserAndPurpose(user.getId(), purpose);

        String rawToken = generateSecureRandomToken();

        String tokenHash = hashToken(rawToken);

        Instant expiresAt = calculateExpirationTime(purpose);

        Token token = new Token();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setPurpose(purpose);
        token.setRequestIp(requestIp);
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(Instant.now());

        tokenRepository.save(token);

        return new TokenResponseDto(rawToken, purpose, expiresAt);
    }

    public Token validateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        var token = tokenRepository.findByTokenHash(tokenHash).orElseThrow(
                () -> new InvalidTokenException("Password Reset Token is invalid")
        );

        if(!token.isValid()) {
            throw new InvalidTokenException("Invalid token");
        }

        return token;
    }

    public void invalidateToken(Token token) {
        token.markAsUsed();
        token.invalidate();
        tokenRepository.save(token);
    }

    private String generateSecureRandomToken() {
        byte[] randomBytes = new byte[tokenConfig.getLength()];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
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

    private Instant calculateExpirationTime(TokenPurpose purpose) {
        long expirationSeconds = switch (purpose) {
            case PASSWORD_RESET -> tokenConfig.getPasswordResetExpirationSeconds();
            case EMAIL_VERIFICATION -> tokenConfig.getEmailVerificationExpirationSeconds();
            case TWO_FACTOR_AUTH -> tokenConfig.getTwoFactorAuthExpirationSeconds();
        };

        return Instant.now().plusSeconds(expirationSeconds);
    }
}