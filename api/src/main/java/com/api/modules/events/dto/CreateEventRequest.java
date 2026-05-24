package com.api.modules.events.dto;

import com.api.common.validators.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

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
        @Future(message = "Start time must be in the future")
        Instant startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        Instant endTime,

        @NotNull(message = "Banner image is required")
        MultipartFile bannerImage,

        Boolean isPublic,

        @Valid
        List<CreateTicketTypeRequest> ticketTypes
) {
}
