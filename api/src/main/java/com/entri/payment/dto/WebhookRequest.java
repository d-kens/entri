package com.entri.payment.dto;

import java.util.Map;

public record WebhookRequest(
        Map<String, String> headers,
        String payload
) {
}
