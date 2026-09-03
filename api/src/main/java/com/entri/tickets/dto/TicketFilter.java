package com.entri.tickets.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TicketFilter(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {
    public TicketFilter {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
    }
}
