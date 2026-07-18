package com.entri.modules.events.dto;

import com.entri.modules.events.entity.EventStatus;

import java.time.Instant;
import java.util.List;

public record EventDetailResponse(
        String externalId,
        String title,
        String description,
        String categoryName,
        Long categoryId,
        String venueName,
        String venueCity,
        String venueCountry,
        Instant startTime,
        Instant endTime,
        String bannerUrl,
        EventStatus status,
        Instant publishedAt,
        Instant dateCreated,
        List<TicketTypeResponse> ticketTypes
) {
}
