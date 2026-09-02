package com.entri.analytics.dto;

import java.math.BigDecimal;

public record OrganizerSummaryMetricsResponse(
        BigDecimal totalRevenue,
        BigDecimal walletBalance,
        String walletCurrency,
        long totalTicketsSold,
        long upcomingEventsCount,
        long liveEventsCount
) {}
