package com.parrcel.api.modules.payment.events;

import com.parrcel.api.modules.payment.enums.PaymentType;

public record PaymentFailedEvent(
        String paymentId,
        PaymentType paymentType,
        String referenceId,
        String failureReason
) {}
