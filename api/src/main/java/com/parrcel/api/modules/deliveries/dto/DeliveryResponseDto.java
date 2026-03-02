package com.parrcel.api.modules.deliveries.dto;

import com.parrcel.api.modules.deliveries.enums.DeliveryStatus;

import java.math.BigDecimal;

public record DeliveryResponseDto(
        String toAgent,
        String fromAgent,
        String toZone,
        String fromZone,
        String externalId,
        String packageName,
        Boolean collectCash,
        String customerName,
        String recipientName,
        String recipientPhone,
        String trackingNumber,
        BigDecimal cashAmount,
        Boolean cashCollected,
        BigDecimal deliveryFee,
        BigDecimal packagePrice,
        String paymentReference,
        String packageDescription,
        DeliveryStatus deliveryStatus
) {}