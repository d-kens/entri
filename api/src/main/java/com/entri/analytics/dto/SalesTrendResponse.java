package com.entri.analytics.dto;

import java.util.List;

public record SalesTrendResponse(
        String period,
        List<SalesTrendDataPoint> data
) {}
