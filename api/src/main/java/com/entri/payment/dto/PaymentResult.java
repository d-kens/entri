package com.entri.payment.dto;

import com.entri.payment.enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * Outcome of a collection, as verified with the payment provider.
 * {@code amount} and {@code currency} are only populated for PAID results.
 */
public record PaymentResult(
        String referenceId,
        PaymentStatus status,
        BigDecimal amount,
        String currency
) {
}
