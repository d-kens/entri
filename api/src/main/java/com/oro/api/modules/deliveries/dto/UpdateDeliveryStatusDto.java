package com.oro.api.modules.deliveries.dto;

import com.oro.api.modules.deliveries.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusDto(
        @NotNull DeliveryStatus status,
        String cancellationReason
) {}
