package com.api.modules.properties.dto;

import java.math.BigDecimal;

public record PropertyResponse(
        String externalKey,
        String propertyName,
        String description,
        Integer rentDueDate,
        String country,
        String city,
        String area,
        BigDecimal latitude,
        BigDecimal longitude,
        String coverImageUrl,
        Boolean isListed
) {}
