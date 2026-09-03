package com.entri.payment.dto;

import com.entri.payment.enums.PaymentStatus;

public record PaymentResult(
        String referenceId,
        PaymentStatus status
) {
}
