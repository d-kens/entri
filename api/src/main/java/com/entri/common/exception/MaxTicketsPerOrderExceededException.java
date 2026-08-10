package com.entri.common.exception;

public class MaxTicketsPerOrderExceededException extends RuntimeException {
    public MaxTicketsPerOrderExceededException(String message) {
        super(message);
    }
}
