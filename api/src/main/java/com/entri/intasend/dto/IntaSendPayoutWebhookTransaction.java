package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntaSendPayoutWebhookTransaction(
        @JsonProperty("transaction_id") String transactionId,
        String status,
        @JsonProperty("status_code") String statusCode
) {
}
