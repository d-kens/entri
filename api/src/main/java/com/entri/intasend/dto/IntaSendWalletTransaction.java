package com.entri.intasend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record IntaSendWalletTransaction(

        String id,

        @JsonProperty("transaction_type")
        String transactionType,

        BigDecimal amount,

        String currency,

        String narrative,

        String status,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("updated_at")
        String updatedAt
) {
}
