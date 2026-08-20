package com.entri.events.dto;

import java.math.BigDecimal;

public record EventTicketReservationItemDto(
        Integer quantity,
        String ticketType,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
