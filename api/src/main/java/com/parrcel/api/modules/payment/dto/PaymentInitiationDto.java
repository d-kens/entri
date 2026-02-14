package com.parrcel.api.modules.payment.dto;

import com.parrcel.api.modules.payment.validation.ValidPaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentInitiationDto(
        @NotBlank(message = "referenceId is required")
        String referenceId,

        @ValidPaymentMethod
        String paymentMethod,

        @NotBlank(message = "phoneNumber is required")
        String phoneNumber,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than 0")
        BigDecimal amount
) {}