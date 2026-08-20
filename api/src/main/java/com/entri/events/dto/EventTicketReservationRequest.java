package com.entri.events.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EventTicketReservationRequest(
        @Valid
        @NotEmpty(message = "At least one ticket item is required")
        List<EventTicketReservationItemRequest> itemRequests
) {}
