package com.entri.tickets.dto;

public record TicketResponse(
        String ticketCode,
        String eventExternalId,
        String eventTitle,
        String ticketTypeName,
        String holderFirstName,
        String holderLastName,
        String status
) {}
