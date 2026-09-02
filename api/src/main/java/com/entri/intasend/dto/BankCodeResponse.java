package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankCodeResponse(
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("bank_code") String bankCode
) {
}
