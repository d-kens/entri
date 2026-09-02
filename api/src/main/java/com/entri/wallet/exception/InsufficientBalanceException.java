package com.entri.wallet.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Insufficient wallet balance to complete this withdrawal");
    }
}
