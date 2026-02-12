package com.parrcel.api.modules.deliveries.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateDeliveryDto(
        @NotNull(message = "toPoint is required")
        Long toPoint,

        @NotNull(message = "fromPoint is required")
        Long fromPoint,

        @NotNull(message = "collectCash is required")
        Boolean collectCash,

        @NotBlank(message = "packageName is required")
        String packageName,

        @NotBlank(message = "recipientName is required")
        String recipientName,

        BigDecimal cashAmount,

        @NotBlank(message = "recipientPhone is required")
        String recipientPhone,

        @NotNull(message = "deliveryFee is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "deliveryFee must be greater than 0")
        BigDecimal deliveryFee,

        @NotNull(message = "packagePrice is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "packagePrice must be greater than 0")
        BigDecimal packagePrice,

        @NotBlank(message = "packageDescription is required")
        String packageDescription
) {}