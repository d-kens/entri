package com.entri.payment;

public record PayoutResult(
        String trackingId,
        PayoutStatus status
) {
}
