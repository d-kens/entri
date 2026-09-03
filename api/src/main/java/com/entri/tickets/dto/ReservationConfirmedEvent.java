package com.entri.tickets.dto;

import java.math.BigDecimal;

public record ReservationConfirmedEvent(
        String reservationExternalId,
        String organizerExternalKey,
        BigDecimal organizerAmount,
        String currency
) {}
