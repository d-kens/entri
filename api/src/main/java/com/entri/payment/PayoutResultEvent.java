package com.entri.payment;

import com.entri.payment.enums.PayoutStatus;

public record PayoutResultEvent(
        String trackingId,
        PayoutStatus status
) {
}
