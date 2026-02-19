package com.parrcel.api.modules.payment.providers.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MpesaStkResponse(
        @JsonProperty("MerchantRequestID") String merchantRequestId,
        @JsonProperty("CheckoutRequestID") String checkoutRequestId,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription,
        @JsonProperty("CustomerMessage") String customerMessage
) {}