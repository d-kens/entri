package com.entri.events.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EventTicketReservationResponse(
        Instant expiresAt,
        String reservationId,
        BigDecimal totalAmount,
        String externalEventId
) {}
