package com.entri.payment;

import com.entri.payment.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResultEvent(
        String referenceId,
        PaymentStatus status,
        BigDecimal amount,
        String currency
) {
}
