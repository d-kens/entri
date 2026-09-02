package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendBankCode(

        @JsonProperty("bank_id")
        String bankId,

        @JsonProperty("bank_name")
        String bankName
) {
}
