package com.entri.intasend.dto;

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

        String channel,

        @JsonProperty("redirect_url")
        String redirectUrl,

        BigDecimal amount,

        String currency,

        @JsonProperty("wallet_id")
        String walletId,

        @JsonProperty("card_tarrif")
        String cardTarrif,

        @JsonProperty("mobile_tarrif")
        String mobileTarrif
) {
}
