package com.entri.modules.payment.dto;

public record CheckoutResponse(
        String paymentId,
        String checkoutURL
) {
}
