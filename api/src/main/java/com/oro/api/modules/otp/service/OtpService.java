package com.oro.api.modules.otp.service;

import com.oro.api.modules.otp.config.OtpConfig;
import com.oro.api.modules.otp.dto.OtpResponseDto;
import com.oro.api.modules.otp.entity.Otp;
import com.oro.api.modules.otp.entity.OtpPurpose;
import com.oro.api.modules.otp.exception.InvalidOtpException;
import com.oro.api.modules.otp.repository.OtpRepository;
import com.oro.api.modules.user.entity.User;
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
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpConfig otpConfig;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public OtpResponseDto generateOtp(User user, String requestIp) {
        otpRepository.invalidateUnusedOtpsByUserAndPurpose(user.getId(), OtpPurpose.PASSWORD_RESET);

        int otpValue = 100_000 + secureRandom.nextInt(900_000);
        String rawOtp = String.valueOf(otpValue);
        Instant expiresAt = Instant.now().plusSeconds(otpConfig.getExpirationSeconds());

        Otp otp = new Otp();
        otp.setUser(user);
        otp.setOtpHash(hashOtp(rawOtp));
        otp.setPurpose(OtpPurpose.PASSWORD_RESET);
        otp.setRequestIp(requestIp);
        otp.setExpiresAt(expiresAt);

        otpRepository.save(otp);

        return new OtpResponseDto(rawOtp, otpConfig.getExpirationSeconds() / 60);
    }

    @Transactional
    public Otp validateOtp(String rawOtp, User user) {
        String otpHash = hashOtp(rawOtp);

        var otp = otpRepository.findValidOtpByUserAndHash(user.getId(), otpHash, OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));

        if (!otp.isValid()) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        return otp;
    }

    @Transactional
    public void invalidateOtp(Otp otp) {
        otp.markAsUsed();
        otp.invalidate();
        otpRepository.save(otp);
    }

    private String hashOtp(String rawOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawOtp.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
