package com.entri.events.dto;

import com.entri.events.entity.EventTicketReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record EventReservationSummaryResponse(
        String reservationId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        BigDecimal totalAmount,
        EventTicketReservationStatus status,
        int ticketCount,
        Instant createdAt
) {}
