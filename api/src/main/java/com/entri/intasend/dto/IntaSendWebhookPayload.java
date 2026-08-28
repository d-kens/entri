package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendWebhookPayload(
        @JsonProperty("invoice_id") String invoiceId,
        String state,
        @JsonProperty("api_ref") String apiRef,
        @JsonProperty("failed_reason") String failedReason,
        @JsonProperty("failed_code") String failedCode,
        String challenge
) {
}
