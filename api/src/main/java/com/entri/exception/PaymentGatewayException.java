package com.entri.exception;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
