package com.oro.api.modules.user.service;

import com.oro.api.modules.token.entity.RefreshTokenSession;
import com.oro.api.modules.token.repository.RefreshTokenSessionRepository;
import com.oro.api.modules.user.dto.AuthRequest;
import com.oro.api.modules.user.dto.AuthResponse;
import com.oro.api.modules.user.entity.User;
import com.oro.api.security.config.JwtConfig;
import com.oro.api.security.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Transactional
    public Optional<AuthResponse> authenticate(AuthRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.phoneNumber(), request.password())
        );

        var user = userService.getUserByPhoneNumber(request.phoneNumber());

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        var rawRefreshToken = refreshToken.toString();

        revokeExistingSessions(user);
        storeRefreshTokenSession(user, rawRefreshToken);
        setRefreshTokenCookie(response, rawRefreshToken);

        return Optional.of(new AuthResponse(
                user.getName(),
                user.getRoles(),
                user.getExternalId(),
                accessToken.toString()
        ));
    }

    private void revokeExistingSessions(User user) {
        var sessions = refreshTokenSessionRepository.findActiveSessions(user.getId());
        sessions.forEach(s -> s.setIsRevoked(true));
        refreshTokenSessionRepository.saveAll(sessions);
    }

    private void storeRefreshTokenSession(User user, String rawRefreshToken) {
        var session = new RefreshTokenSession();
        session.setUser(user);
        session.setRefreshTokenHash(hashToken(rawRefreshToken));
        session.setIssuedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration()));
        session.setIsRevoked(false);
        refreshTokenSessionRepository.save(session);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawRefreshToken) {
        var cookie = ResponseCookie.from("refreshToken", rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtConfig.getRefreshTokenExpiration())
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String hashToken(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
