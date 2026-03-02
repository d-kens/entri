package com.parrcel.api.modules.payment.enums;

public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED;

    public boolean isSuccessful() {
        return this == PAID;
    }

    public boolean canBeRefunded() {
        return this == PAID;
    }
}

