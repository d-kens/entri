package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IntaSendSendMoneyWebhookPayload(
        @JsonProperty("file_id") String fileId,
        @JsonProperty("tracking_id") String trackingId,
        @JsonProperty("batch_reference") String batchReference,
        String status,
        @JsonProperty("status_code") String statusCode,
        List<Transaction> transactions,
        String challenge
) {
    public record Transaction(
            @JsonProperty("transaction_id") String transactionId,
            String status,
            @JsonProperty("status_code") String statusCode,
            @JsonProperty("status_description") String statusDescription,
            @JsonProperty("request_reference_id") String requestReferenceId
    ) {}
}
