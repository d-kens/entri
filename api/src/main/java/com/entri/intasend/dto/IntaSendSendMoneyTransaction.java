package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IntaSendSendMoneyTransaction(
        String name,
        String account,
        @JsonProperty("account_type") String accountType,
        @JsonProperty("account_reference") String accountReference,
        @JsonProperty("bank_code") String bankCode,
        String amount,
        String narrative
) {
}
