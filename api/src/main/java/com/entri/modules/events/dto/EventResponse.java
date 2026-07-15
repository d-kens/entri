package com.entri.modules.events.dto;

import com.entri.modules.events.entity.EventStatus;

import java.time.Instant;

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
        Instant publishedAt,
        Instant dateCreated
) {
}
