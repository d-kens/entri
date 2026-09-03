package com.entri.wallet.dto;

import com.entri.wallet.WalletTransactionStatus;
import com.entri.wallet.WalletTransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionResponse(
        String externalId,
        WalletTransactionType type,
        BigDecimal amount,
        String currency,
        String referenceId,
        WalletTransactionStatus status,
        Instant dateCreated
) {
}
