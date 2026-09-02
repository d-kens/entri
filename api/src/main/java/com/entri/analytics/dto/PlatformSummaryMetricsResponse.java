package com.entri.analytics.dto;

import java.math.BigDecimal;

public record PlatformSummaryMetricsResponse(
        long totalOrganizers,
        long totalEvents,
        long publishedEvents,
        long liveEventsCount,
        long totalTicketsSold,
        BigDecimal totalGmv
) {}
