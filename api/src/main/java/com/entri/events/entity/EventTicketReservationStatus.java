package com.entri.events.entity;

public enum EventTicketReservationStatus {
    PENDING,
    CONFIRMED,
    EXPIRED,
    /** Paid after the hold expired and the tickets could no longer be issued. The buyer must be refunded. */
    REFUND_REQUIRED
}
