package com.parrcel.api.modules.token.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "token")
public class TokenConfig {

    private int length = 64;

    private long passwordResetExpirationSeconds = 3600;

    private long emailVerificationExpirationSeconds = 86400;

    private long twoFactorAuthExpirationSeconds = 300;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getPasswordResetExpirationSeconds() {
        return passwordResetExpirationSeconds;
    }

    public void setPasswordResetExpirationSeconds(long passwordResetExpirationSeconds) {
        this.passwordResetExpirationSeconds = passwordResetExpirationSeconds;
    }

    public long getEmailVerificationExpirationSeconds() {
        return emailVerificationExpirationSeconds;
    }

    public void setEmailVerificationExpirationSeconds(long emailVerificationExpirationSeconds) {
        this.emailVerificationExpirationSeconds = emailVerificationExpirationSeconds;
    }

    public long getTwoFactorAuthExpirationSeconds() {
        return twoFactorAuthExpirationSeconds;
    }

    public void setTwoFactorAuthExpirationSeconds(long twoFactorAuthExpirationSeconds) {
        this.twoFactorAuthExpirationSeconds = twoFactorAuthExpirationSeconds;
    }
}