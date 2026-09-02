package com.entri.analytics.dto;

import java.math.BigDecimal;

public record OrganizerSummaryMetricsResponse(
        BigDecimal totalRevenue,
        long totalTicketsSold,
        long upcomingEventsCount,
        long liveEventsCount
) {}
