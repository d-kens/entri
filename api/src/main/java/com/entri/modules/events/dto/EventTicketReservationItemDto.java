package com.entri.modules.events.dto;

import java.math.BigDecimal;

public record EventTicketReservationItemDto(
        Integer quantity,
        String ticketType,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
