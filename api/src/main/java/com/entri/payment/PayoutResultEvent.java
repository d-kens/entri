package com.entri.payment;

public record PayoutResultEvent(
        String trackingId,
        PayoutStatus status
) {
}
