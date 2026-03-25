package com.parrcel.api.modules.wallet.dto;

import com.parrcel.api.common.validators.ValidPhoneNumber;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "100.00", message = "Minimum withdrawal is KSh 100")
        BigDecimal amount,

        @ValidPhoneNumber
        String phoneNumber

) {}
