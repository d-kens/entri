package com.entri.payment;

public record PaymentResultEvent(
        String referenceId,
        PaymentStatus status
) {
}
