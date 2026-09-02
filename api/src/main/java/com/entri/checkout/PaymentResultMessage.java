package com.entri.checkout;

import com.entri.payment.PaymentStatus;

public record PaymentResultMessage(
        String referenceId,
        PaymentStatus status
) {
}
