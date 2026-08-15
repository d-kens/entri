package com.entri.modules.events.dto;

import com.entri.modules.events.entity.EventTicketReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EventTicketReservationDetailDto(
        Instant expiresAt,
        String reservationId,
        BigDecimal totalAmount,
        String externalEventId,
        EventTicketReservationStatus status,
        List<EventTicketReservationItemDto> reservationItems
) {
}
