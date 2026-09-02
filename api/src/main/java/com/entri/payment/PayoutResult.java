package com.entri.payment;

public record PayoutResult(
        String trackingId,
        boolean completed
) {
}
