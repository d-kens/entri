package com.entri.tickets.dto;

import java.time.Instant;

public record CheckInCodeResponse(
        String code,
        String eventExternalId,
        Instant expiresAt
) {}
