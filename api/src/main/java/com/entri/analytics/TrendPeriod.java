package com.entri.analytics;

import com.entri.exception.BadRequestException;

public enum TrendPeriod {
    SEVEN_DAYS("7d", 7, false),
    THIRTY_DAYS("30d", 30, false),
    TWELVE_MONTHS("12m", 12, true);

    public final String value;
    public final int count;
    public final boolean monthly;

    TrendPeriod(String value, int count, boolean monthly) {
        this.value = value;
        this.count = count;
        this.monthly = monthly;
    }

    public String getValue() {
        return value;
    }

    public static TrendPeriod fromString(String s) {
        for (TrendPeriod p : values()) {
            if (p.value.equals(s)) return p;
        }
        throw new BadRequestException("Invalid period. Allowed values: 7d, 30d, 12m");
    }
}
