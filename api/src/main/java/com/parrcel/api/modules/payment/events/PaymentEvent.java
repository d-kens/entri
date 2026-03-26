package com.parrcel.api.modules.payment.events;

import com.parrcel.api.modules.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentEvent(
        String paymentId,
        String referenceId,
        PaymentStatus status,
        BigDecimal amount,
        String providerReference,
        String failureReason,
        LocalDateTime transactionDate,
        LocalDateTime timestamp
) {
    public static PaymentEvent success(
            String paymentId,
            String referenceId,
            BigDecimal amount,
            String providerReference,
            LocalDateTime transactionDate
    ) {
        return new PaymentEvent(
                paymentId,
                referenceId,
                PaymentStatus.SUCCESS,
                amount,
                providerReference,
                null,
                transactionDate,
                LocalDateTime.now()
        );
    }

    public static PaymentEvent failure(
            String paymentId,
            String referenceId,
            String failureReason
    ) {
        return new PaymentEvent(
                paymentId,
                referenceId,
                PaymentStatus.FAILED,
                null,
                null,
                failureReason,
                null,
                LocalDateTime.now()
        );
    }

    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    public boolean isFailure() {
        return status == PaymentStatus.FAILED;
    }
}