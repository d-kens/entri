package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendPayoutWebhookTransaction(
        @JsonProperty("transaction_id") String transactionId,
        String status,
        @JsonProperty("status_code") String statusCode
) {
}
