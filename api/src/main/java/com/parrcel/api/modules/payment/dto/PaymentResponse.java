package com.parrcel.api.modules.payment.dto;

public record PaymentResponse(
        boolean success,
        String message,
        String paymentId,
        String providerReference
) {}
