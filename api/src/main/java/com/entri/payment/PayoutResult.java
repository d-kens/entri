package com.entri.payment;

import com.entri.payment.enums.PayoutStatus;

public record PayoutResult(
        String trackingId,
        PayoutStatus status
) {
}
