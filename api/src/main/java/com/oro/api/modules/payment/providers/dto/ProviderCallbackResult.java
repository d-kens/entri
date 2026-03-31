package com.oro.api.modules.payment.providers.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProviderCallbackResult(
        boolean success,
        String providerTransactionId,
        String providerReference,
        BigDecimal amount,
        LocalDateTime transactionDate,
        String phoneNumber,
        String failureReason
) {}
