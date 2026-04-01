package com.oro.api.modules.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
public class TwoFactorCode {
    @Column(name = "two_factor_code")
    private String twoFactorCode;

    @Column(name = "two_factor_code_expiration")
    private Instant twoFactorCodeExpirationDate;

    public void generate(int minutesValid) {
        int otp = (int) (Math.random() * 900000) + 100000;
        this.twoFactorCode = String.valueOf(otp);
        this.twoFactorCodeExpirationDate = Instant.now().plusSeconds(minutesValid * 60);
    }

    public boolean verify(String input) {
        if (twoFactorCode == null || isExpired()) return false;
        boolean valid = twoFactorCode.equals(input);
        if (valid) invalidate();
        return valid;
    }

    public boolean isExpired() {
        return twoFactorCodeExpirationDate == null || Instant.now().isAfter(twoFactorCodeExpirationDate);
    }

    public void invalidate() {
        this.twoFactorCode = null;
        this.twoFactorCodeExpirationDate = null;
    }

    public void invalidateAfterUsage() {
        this.twoFactorCode = null;
        this.twoFactorCodeExpirationDate = null;
    }
}
