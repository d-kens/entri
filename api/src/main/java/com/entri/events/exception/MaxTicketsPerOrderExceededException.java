package com.entri.events.exception;

public class MaxTicketsPerOrderExceededException extends RuntimeException {
    public MaxTicketsPerOrderExceededException(String message) {
        super(message);
    }
}
