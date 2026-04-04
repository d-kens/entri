package com.oro.api.modules.user.service;

import com.oro.api.modules.notification.enums.NotificationType;
import com.oro.api.modules.notification.events.SendNotificationEvent;
import com.oro.api.modules.otp.service.OtpService;
import com.oro.api.modules.user.entity.RefreshTokenSession;
import com.oro.api.modules.user.exception.InvalidTokenException;
import com.oro.api.modules.user.repository.RefreshTokenSessionRepository;
import com.oro.api.modules.user.dto.AuthRequest;
import com.oro.api.modules.user.dto.AuthResponse;
import com.oro.api.modules.user.dto.ChangePasswordRequest;
import com.oro.api.modules.user.dto.ResetPasswordRequest;
import com.oro.api.modules.user.entity.User;
import com.oro.api.security.config.CookieConfig;
import com.oro.api.security.config.JwtConfig;
import com.oro.api.security.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final CookieConfig cookieConfig;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final OtpService otpService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponse authenticate(AuthRequest request, HttpServletResponse response) {
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

        return new AuthResponse(
                user.getName(),
                user.getRoles(),
                user.getExternalId(),
                accessToken.toString()
        );
    }

    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        var rawRefreshToken = extractRefreshTokenCookie(request);
        var tokenHash = hashToken(rawRefreshToken);

        var session = refreshTokenSessionRepository
                .findByRefreshTokenHashAndIsRevokedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setIsRevoked(true);
            refreshTokenSessionRepository.save(session);
            throw new InvalidTokenException("Refresh token has expired");
        }

        var user = session.getUser();
        var newAccessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(
                user.getName(),
                user.getRoles(),
                user.getExternalId(),
                newAccessToken.toString()
        );
    }

    @Transactional
    public void logout(User user, HttpServletResponse response) {
        revokeExistingSessions(user);

        var expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }

    @Transactional
    public void forgotPassword(String phoneNumber, String requestIp) {
        var user = userService.getUserByPhoneNumber(phoneNumber);
        var otpResponse = otpService.generateOtp(user, requestIp);
        eventPublisher.publishEvent(new SendNotificationEvent(user, Map.of(
                "userName", user.getName(),
                "code", otpResponse.otp(),
                "expiresIn", otpResponse.expiresInMinutes() + " minutes"
        ), NotificationType.RESET_PASSWORD));
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        userService.changePassword(user.getPhoneNumber(), request.currentPassword(), request.newPassword());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var user = userService.getUserByPhoneNumber(request.phoneNumber());
        var otp = otpService.validateOtp(request.otp(), user);
        userService.resetPassword(request.phoneNumber(), request.newPassword());
        otpService.invalidateOtp(otp);
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new InvalidTokenException("No refresh token cookie present");
        }
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new InvalidTokenException("No refresh token cookie present");
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
                .secure(cookieConfig.isSecure())
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
