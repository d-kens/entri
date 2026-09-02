package com.entri.events.dto;

import java.util.List;

public record EventCheckInStatsResponse(
        String eventExternalId,
        long totalTickets,
        long checkedIn,
        double checkInPercentage,
        List<RecentCheckInDto> recentCheckIns
) {}
