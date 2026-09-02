package com.entri.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawRequest(

        @NotNull(message = "Withdrawal type is required")
        WithdrawType type,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Account is required")
        String account,

        String accountNumber,

        String bankCode,

        @NotBlank(message = "Recipient name is required")
        String recipientName
) {
}
