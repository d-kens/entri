package com.entri.payment.dto;

import com.entri.payment.PaymentStatus;

public record PaymentResult(
        String referenceId,
        PaymentStatus status
) {
}
