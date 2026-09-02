package com.entri.tickets.dto;

import java.time.Instant;

public record TicketResponse(
        String ticketExternalId,
        String ticketCode,
        String reservationExternalId,
        String eventExternalId,
        String eventTitle,
        String ticketTypeName,
        String holderFirstName,
        String holderLastName,
        String status,
        Instant checkedInAt
) {}
