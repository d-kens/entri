package com.api.modules.events.dto;

import com.api.modules.events.entity.EventStatus;

import java.time.Instant;
import java.util.List;

public record EventResponse(
        String externalId,
        String title,
        String description,
        String categoryName,
        String venueName,
        String venueCity,
        String venueCountry,
        Instant startTime,
        Instant endTime,
        String bannerUrl,
        EventStatus status,
        boolean isPublic,
        Instant publishedAt,
        Instant dateCreated,
        List<TicketTypeResponse> ticketTypes
) {
}
