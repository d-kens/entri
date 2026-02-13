package com.parrcel.api.modules.deliveries.dto;

import com.parrcel.api.modules.deliveries.enums.DeliveryStatus;
import com.parrcel.api.modules.deliveries.enums.PaymentMethod;
import com.parrcel.api.modules.deliveries.enums.PaymentStatus;

import java.math.BigDecimal;

public record DeliveryResponseDto(
        String toAgent,
        String fromAgent,
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
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        DeliveryStatus deliveryStatus
) {}