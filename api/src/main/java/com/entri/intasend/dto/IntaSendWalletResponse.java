package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record IntaSendWalletResponse(

        @JsonProperty("wallet_id")
        String walletId,

        String label,

        String currency,

        @JsonProperty("can_disburse")
        boolean canDisburse,

        @JsonProperty("current_balance")
        BigDecimal currentBalance,

        @JsonProperty("available_balance")
        BigDecimal availableBalance
) {
}
