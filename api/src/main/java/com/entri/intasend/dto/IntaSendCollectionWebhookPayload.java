package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntaSendCollectionWebhookPayload(
        @JsonProperty("invoice_id") String invoiceId,
        String state,
        @JsonProperty("api_ref") String apiRef,
        @JsonProperty("failed_reason") String failedReason,
        @JsonProperty("failed_code") String failedCode,
        String challenge,
        String provider,
        String currency,
        String account,
        String value,
        String charges,
        @JsonProperty("net_amount") String netAmount
) {
}
