package com.parrcel.api.modules.payment.providers.dto.mpesa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MpesaParseCallbackResult(
        boolean success,
        String providerTransactionId,
        String providerReference,
        BigDecimal amount,
        LocalDateTime transactionDate,
        String phoneNumber,
        String failureReason
) {}
