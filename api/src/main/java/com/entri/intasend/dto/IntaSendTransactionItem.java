package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IntaSendTransactionItem(

        String name,

        String account,

        @JsonProperty("account_number")
        String accountNumber,

        @JsonProperty("bank_code")
        String bankCode,

        BigDecimal amount,

        String narrative
) {
}
