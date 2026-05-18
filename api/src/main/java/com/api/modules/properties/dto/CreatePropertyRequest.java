package com.api.modules.properties.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePropertyRequest(
        @NotBlank(message = "Property name is required")
        String propertyName,
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Rent due date is required")
        @Min(value = 1, message = "Rent due date must be between 1 and 31")
        @Max(value = 31, message = "Rent due date must be between 1 and 31")
        Integer rentDueDate,
        Integer gracePeriodDate,
        @NotBlank(message ="Country is required")
        String country,
        @NotBlank(message ="City is required")
        String city,
        @NotBlank(message ="Area is required")
        String area,
        @NotNull(message = "Latitude is required")
        BigDecimal latitude,
        @NotNull(message = "Longitude is required")
        BigDecimal longitude,
//        @NotNull(message = "Cover image is required")
//        MultipartFile coverImage,
        Boolean isListed
) {}
