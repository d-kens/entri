package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntaSendPaymentStatusResponse(Invoice invoice) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Invoice(
            @JsonProperty("invoice_id") String invoiceId,
            String state,
            @JsonProperty("api_ref") String apiRef,
            String currency,
            String value
    ) {
    }
}
