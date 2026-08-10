package com.entri.common.exception;

public class TicketTypeNotForEventException extends RuntimeException {
    public TicketTypeNotForEventException(String message) {
        super(message);
    }
}
