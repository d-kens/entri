package com.entri.payment;

import java.math.BigDecimal;

public record PayoutRequest(
        String recipientName,
        String account,
        String accountReference,
        String bankCode,
        BigDecimal amount,
        String currency,
        PayoutMethod method,
        String idempotencyKey
) {
}
