package com.oro.api.modules.payment.providers.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MpesaAuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") String expiresIn
) {}
