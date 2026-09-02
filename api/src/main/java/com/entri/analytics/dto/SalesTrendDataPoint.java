package com.entri.analytics.dto;

import java.math.BigDecimal;

public record SalesTrendDataPoint(
        String date,
        long ticketsSold,
        BigDecimal revenue
) {}
