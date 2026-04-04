package com.oro.api.modules.routes.dto;

import com.oro.api.modules.routes.entity.BatchStatus;

import java.util.List;

public record BatchResponseDto(
        String externalId,
        String routeName,
        BatchStatus status,
        String driverName,
        String driverPhone,
        List<BatchDeliveryDto> deliveries
) {
    public record BatchDeliveryDto(
            String externalId,
            String trackingNumber,
            String recipientName,
            String recipientPhone,
            String toAgentName,
            String toAgentAddress
    ) {}
}
