package com.parrcel.api.modules.wallet.dto;

import com.parrcel.api.modules.wallet.entity.TransactionStatus;
import com.parrcel.api.modules.wallet.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResponse(
        String externalId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        TransactionStatus status,
        String referenceType,
        String referenceId,
        String description,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}
