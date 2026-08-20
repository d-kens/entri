package com.entri.events.dto;

import com.entri.events.entity.EventStatus;

import java.time.Instant;

public record EventResponse(
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
        String currency,
        EventStatus status,
        Instant publishedAt,
        Instant dateCreated
) {
}
