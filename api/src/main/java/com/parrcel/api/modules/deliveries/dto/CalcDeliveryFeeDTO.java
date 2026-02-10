package com.parrcel.api.modules.deliveries.dto;

import jakarta.validation.constraints.NotNull;

public record CalcDeliveryFeeDTO(
        @NotNull(message = "fromPoint is required")
        Long fromPoint,

        @NotNull(message = "toPoint is required")
        Long toPoint
) {}