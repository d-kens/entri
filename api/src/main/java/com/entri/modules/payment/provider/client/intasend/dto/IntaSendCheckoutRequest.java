package com.entri.modules.payment.provider.client.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record IntaSendCheckoutRequest(

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("phone_number")
        String phoneNumber,

        String email,

        @JsonProperty("api_ref")
        String apiRef,

        String method,

        String channel,

        @JsonProperty("redirect_url")
        String redirectUrl,

        BigDecimal amount,

        String currency
) {
}