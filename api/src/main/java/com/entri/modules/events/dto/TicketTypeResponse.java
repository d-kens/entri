package com.entri.modules.events.dto;

import com.entri.modules.events.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketTypeResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        Integer quantity,
        Integer soldQuantity,
        Integer reservedQuantity,
        Integer maxTicketsPerOrder,
        Instant saleStartDate,
        Instant saleEndDate,
        TicketStatus status
) {
}
