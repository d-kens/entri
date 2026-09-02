package com.entri.events.dto;

import java.time.Instant;

public record RecentCheckInDto(
        String ticketCode,
        String ticketTypeName,
        Instant checkedInAt
) {}
