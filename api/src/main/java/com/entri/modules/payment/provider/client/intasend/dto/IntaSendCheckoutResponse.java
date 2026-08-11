package com.entri.modules.payment.provider.client.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendCheckoutResponse(
        String id,
        String url,

        @JsonProperty("api_ref")
        String apiReference,

        String amount,
        String currency,
        boolean paid
) {
}
