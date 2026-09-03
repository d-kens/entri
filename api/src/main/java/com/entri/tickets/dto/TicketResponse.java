package com.entri.tickets.dto;

import java.time.Instant;

public record TicketResponse(
        String ticketExternalId,
        String ticketCode,
        String reservationExternalId,
        String eventExternalId,
        String eventTitle,
        Instant eventStartTime,
        Instant eventEndTime,
        String venueName,
        String venueCity,
        String currency,
        String ticketTypeName,
        java.math.BigDecimal ticketPrice,
        String holderFirstName,
        String holderLastName,
        String status,
        Instant checkedInAt
) {}
