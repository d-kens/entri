package com.entri.events.exception;

public class EventNotOnSaleException extends RuntimeException {
    public EventNotOnSaleException(String message) {
        super(message);
    }
}
