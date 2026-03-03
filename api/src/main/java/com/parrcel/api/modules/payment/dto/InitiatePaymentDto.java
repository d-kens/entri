package com.parrcel.api.modules.payment.dto;

import com.parrcel.api.modules.payment.enums.PayableType;
import com.parrcel.api.modules.payment.validation.ValidInitiatePaymentDto;
import com.parrcel.api.modules.payment.validation.ValidPaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@ValidInitiatePaymentDto
public record InitiatePaymentDto(
        String phoneNumber,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "paymentDescription is required")
        String paymentDescription,

        PayableType payableType,

        @NotBlank(message = "payableId is required")
        String payableId,

        @ValidPaymentMethod
        @NotBlank(message = "paymentMethod is required")
        String paymentMethod
) {}