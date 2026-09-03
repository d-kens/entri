package com.entri.payment.dto;

import java.math.BigDecimal;

public record CheckoutRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String reference,
        String redirectUrl,
        BigDecimal amount,
        String currency
) {
}
