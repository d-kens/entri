package com.entri.checkout;

import com.entri.payment.PaymentStatus;

public record CheckoutResultMessage(
        String referenceId,
        PaymentStatus status
) {
}
