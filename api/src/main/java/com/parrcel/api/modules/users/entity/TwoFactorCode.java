package com.parrcel.api.modules.users.entity;

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
    private String code;
    private Instant expirationDate;

    public void generate(int minutesValid) {
        int otp = (int) (Math.random() * 900000) + 100000;
        this.code = String.valueOf(otp);
        this.expirationDate = Instant.now().plusSeconds(minutesValid * 60);
    }

    public boolean verify(String input) {
        if (code == null || isExpired()) return false;
        boolean valid = code.equals(input);
        if (valid) invalidate();
        return valid;
    }

    public boolean isExpired() {
        return expirationDate == null || Instant.now().isAfter(expirationDate);
    }

    public void invalidate() {
        this.code = null;
        this.expirationDate = null;
    }

    public void invalidateAfterUsage() {
        this.code = null;
        this.expirationDate = null;
    }
}
