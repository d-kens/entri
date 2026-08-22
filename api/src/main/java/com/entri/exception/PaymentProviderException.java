package com.entri.exception;

public class PaymentProviderException extends RuntimeException {
    public PaymentProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
