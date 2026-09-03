package com.entri.payment;

import com.entri.payment.enums.PaymentStatus;

public record PaymentResultEvent(
        String referenceId,
        PaymentStatus status
) {
}
