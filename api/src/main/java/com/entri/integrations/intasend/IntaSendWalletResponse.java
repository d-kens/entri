package com.entri.integrations.intasend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendWalletResponse(

        @JsonProperty("wallet_id")
        String walletId,

        String label,

        String currency,

        @JsonProperty("can_disburse")
        boolean canDisburse,

        @JsonProperty("current_balance")
        java.math.BigDecimal currentBalance,

        @JsonProperty("available_balance")
        java.math.BigDecimal availableBalance
) {
}
