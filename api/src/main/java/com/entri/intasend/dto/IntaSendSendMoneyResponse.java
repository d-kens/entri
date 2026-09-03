package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendSendMoneyResponse(
        @JsonProperty("file_id") String fileId,
        @JsonProperty("tracking_id") String trackingId,
        String status
) {
}
