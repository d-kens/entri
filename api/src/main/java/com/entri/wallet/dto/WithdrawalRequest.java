package com.entri.wallet.dto;

import com.entri.payment.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull @DecimalMin(value = "100", message = "Minimum withdrawal amount is KES 100") BigDecimal amount,
        @NotNull AccountType accountType,
        @NotBlank String account,
        @NotBlank String name,
        @NotBlank String narrative,
        String accountReference,
        String bankCode
) {
}
