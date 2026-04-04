package com.oro.api.modules.deliveries.dto;

import com.oro.api.modules.deliveries.entity.DeliveryStatus;
import com.oro.api.modules.payment.entity.PaymentStatus;

import java.math.BigDecimal;

public record DeliveryResponseDto(
        String toAgent,
        String fromAgent,
        String toZone,
        String fromZone,
        String externalId,
        String packageName,
        String customerName,
        String recipientName,
        String recipientPhone,
        String trackingNumber,
        BigDecimal deliveryFee,
        BigDecimal packagePrice,
        String packageDescription,
        DeliveryStatus deliveryStatus,
        PaymentStatus paymentStatus
) {}