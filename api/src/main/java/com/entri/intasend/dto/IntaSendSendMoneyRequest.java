package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IntaSendSendMoneyRequest(
        String currency,
        String provider,
        String country,
        @JsonProperty("requires_approval") String requiresApproval,
        @JsonProperty("callback_url") String callbackUrl,
        List<IntaSendSendMoneyTransaction> transactions
) {
}
