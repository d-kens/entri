package com.entri.wallet.dto;

import java.math.BigDecimal;

public record WalletTransactionResponse(
        String id,
        String type,
        BigDecimal amount,
        String currency,
        String narrative,
        String status,
        String createdAt
) {
}
