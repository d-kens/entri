package com.entri.payment.dto;

public record CheckoutResponse(
        String paymentId,
        String checkoutURL
) {
}
