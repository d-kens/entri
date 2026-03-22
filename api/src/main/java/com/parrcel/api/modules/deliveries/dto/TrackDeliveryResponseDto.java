package com.parrcel.api.modules.deliveries.dto;

import com.parrcel.api.modules.deliveries.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.util.List;

public record TrackDeliveryResponseDto(
        String trackingNumber,
        String packageName,
        BigDecimal packagePrice,
        DeliveryStatus status,
        String recipientName,
        String recipientPhone,
        boolean isCollectCash,
        BigDecimal cashAmount,
        LocationInfo from,
        LocationInfo to,
        List<TrackingTimeline> timeline

) {
    public record LocationInfo(
            String zone,
            String point
    ) {}

    public record TrackingTimeline(
            String status,
            String timeStamp,
            String location,
            boolean completed
    ) {}
}
