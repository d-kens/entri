package com.parrcel.api.modules.payment.dto;

import com.parrcel.api.common.validators.ValidPhoneNumber;
import com.parrcel.api.modules.payment.entity.PaymentType;
import com.parrcel.api.modules.payment.validation.ValidPaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InitiatePaymentRequest(
        @ValidPhoneNumber
        String phoneNumber,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "paymentDescription is required")
        String paymentDescription,

        PaymentType paymentType,

        @NotBlank(message = "referenceId is required")
        String referenceId,

        @ValidPaymentMethod
        @NotBlank(message = "paymentMethod is required")
        String paymentMethod
) {}