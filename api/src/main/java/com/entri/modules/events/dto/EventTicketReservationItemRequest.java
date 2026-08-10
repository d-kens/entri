package com.entri.modules.events.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EventTicketReservationItemRequest(
        @NotNull(message = "Ticket type ID is required")
        Long ticketTypeId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}
