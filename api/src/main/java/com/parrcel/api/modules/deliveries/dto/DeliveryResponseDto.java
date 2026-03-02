package com.parrcel.api.modules.deliveries.dto;

import com.parrcel.api.modules.deliveries.enums.DeliveryStatus;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.deliveries.enums.PaymentStatus;

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