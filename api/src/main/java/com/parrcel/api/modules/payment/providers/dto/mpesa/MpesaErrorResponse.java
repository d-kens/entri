package com.parrcel.api.modules.payment.providers.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MpesaErrorResponse(
        @JsonProperty("requestId") String requestId,
        @JsonProperty("errorCode") String errorCode,
        @JsonProperty("errorMessage") String errorMessage
) {}
