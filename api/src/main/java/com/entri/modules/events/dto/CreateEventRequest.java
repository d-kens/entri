package com.entri.modules.events.dto;

import com.entri.common.validators.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

@ValidDateRange(startField = "startTime", endField = "endTime")
public record CreateEventRequest(
        @NotBlank(message = "Event title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotBlank(message = "Venue name is required")
        String venueName,

        @NotBlank(message = "Venue country is required")
        String venueCountry,

        @NotBlank(message = "Venue city is required")
        String venueCity,

        @NotNull(message = "Start time is required")
        Instant startTime,

        @NotNull(message = "End time is required")
        Instant endTime,

        @NotBlank(message = "Banner image is required")
        String bannerUrl,

        Boolean isPublic,

        @Valid
        List<CreateTicketTypeRequest> ticketTypes
) {
}
