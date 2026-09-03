package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IntaSendPayoutWebhookPayload(
        @JsonProperty("tracking_id") String trackingId,
        String status,
        String challenge,
        List<IntaSendPayoutWebhookTransaction> transactions
) {
}
