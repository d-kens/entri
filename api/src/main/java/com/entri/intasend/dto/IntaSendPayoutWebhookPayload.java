package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntaSendPayoutWebhookPayload(
        @JsonProperty("tracking_id") String trackingId,
        String status,
        String challenge,
        List<IntaSendPayoutWebhookTransaction> transactions
) {
}
