package com.api.modules.events.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTicketTypeRequest(

        @NotBlank(message = "Ticket name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Price must be 0.00 or greater")
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Min(value = 1, message = "Max per order must be at least 1")
        Integer maxPerOrder,

        Instant saleStartDate,

        Instant saleEndDate,

        Integer displayOrder,

        Boolean isHidden
) {
}
