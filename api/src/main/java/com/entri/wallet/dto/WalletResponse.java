package com.entri.wallet.dto;

import java.math.BigDecimal;

public record WalletResponse(
        String externalId,
        BigDecimal balance
) {
}
