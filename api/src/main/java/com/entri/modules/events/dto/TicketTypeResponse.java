package com.entri.modules.events.dto;

import com.entri.modules.events.entity.TicketTypeAvailabilityStatus;
import com.entri.modules.events.entity.TicketTypeSaleStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketTypeResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        Integer availableQuantity,
        Integer maxTicketsPerOrder,
        Instant saleStartDate,
        Instant saleEndDate,
        TicketTypeSaleStatus saleStatus,
        TicketTypeAvailabilityStatus availabilityStatus
) {
}
