package com.entri.checkout.dto;

import com.entri.checkout.enums.PaymentStatus;

public record PaymentResultMessage(
        String reservationExternalId,
        PaymentStatus status
) {
}
