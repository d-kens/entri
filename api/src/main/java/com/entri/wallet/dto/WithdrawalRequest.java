package com.entri.wallet.dto;

import com.entri.payment.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull AccountType accountType,
        @NotBlank String account,
        @NotBlank String name,
        @NotBlank String narrative,
        String accountReference,
        String bankCode
) {
}
