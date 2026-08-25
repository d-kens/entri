package com.entri.checkout.dto;

import com.entri.checkout.enums.PaymentStatus;

public record PaymentResult(
        String reservationExternalId,
        PaymentStatus status
) {
}
