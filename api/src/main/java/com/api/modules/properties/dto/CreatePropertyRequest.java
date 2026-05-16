package com.api.modules.properties.dto;

import java.math.BigDecimal;

public record CreatePropertyRequest(
        String propertyName,
        String description,
        Integer rentDueDate,
        Integer gracePeriodDate,
        String country,
        String city,
        String area,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String coverImageUrl,
        Boolean isListed
) {}
