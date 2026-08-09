package com.entri.common.exception;

public class TicketTypeNotAvailableException extends RuntimeException {
    public TicketTypeNotAvailableException(String message) {
        super(message);
    }
}
