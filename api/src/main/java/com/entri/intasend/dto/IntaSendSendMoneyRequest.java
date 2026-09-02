package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IntaSendSendMoneyRequest(

        String currency,

        List<IntaSendTransactionItem> transactions,

        @JsonProperty("wallet_id")
        String walletId,

        @JsonProperty("requires_approval")
        String requiresApproval
) {
}
