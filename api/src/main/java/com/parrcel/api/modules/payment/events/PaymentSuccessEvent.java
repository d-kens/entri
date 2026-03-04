package com.parrcel.api.modules.payment.events;

import com.parrcel.api.modules.payment.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSuccessEvent(
        String paymentId,
        PaymentType paymentType,
        String referenceId,
        BigDecimal amount,
        LocalDateTime completedAt
) {}
