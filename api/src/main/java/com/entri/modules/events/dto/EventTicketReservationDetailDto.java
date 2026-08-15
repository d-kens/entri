package com.entri.modules.events.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EventTicketReservationDetailDto(
        Instant expiresAt,
        String reservationId,
        BigDecimal totalAmount,
        String externalEventId,
        List<EventTicketReservationItemDto> reservationItems
) {
}
