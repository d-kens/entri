package com.parrcel.api.modules.payment.enums;

public enum PaymentMethod {
    MPESA,
    CARD,
    WALLET;

    public String getDisplayName() {
        return switch (this) {
            case MPESA -> "M-Pesa";
            case CARD -> "Card Payment";
            case WALLET -> "Wallet Balance";
        };
    }
}
