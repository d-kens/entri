package com.entri.payment.dto;

import com.entri.payment.AccountType;

import java.math.BigDecimal;

public record PayoutRequest(
        String name,
        String account,
        AccountType accountType,
        String accountReference,
        String bankCode,
        BigDecimal amount,
        String currency,
        String narrative,
        String referenceId
) {
}
