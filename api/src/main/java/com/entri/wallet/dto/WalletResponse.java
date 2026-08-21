package com.entri.wallet.dto;

import java.math.BigDecimal;

public record WalletResponse(
        String walletId,
        String label,
        String currency,
        BigDecimal currentBalance,
        BigDecimal availableBalance
) {
}
