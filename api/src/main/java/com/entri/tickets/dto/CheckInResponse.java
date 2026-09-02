package com.entri.tickets.dto;

import java.time.Instant;

public record CheckInResponse(
        CheckInResult result,
        String holderName,
        String ticketType,
        Instant checkedInAt
) {}
