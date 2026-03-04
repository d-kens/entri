package com.parrcel.api.modules.payment.dto;

public record InitiatePaymentResponse(
        String paymentId,
        String referenceId
) {}
