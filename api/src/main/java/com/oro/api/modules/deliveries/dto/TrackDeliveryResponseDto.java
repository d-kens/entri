package com.oro.api.modules.deliveries.dto;

import com.oro.api.modules.deliveries.entity.DeliveryStatus;

import java.math.BigDecimal;
import java.util.List;

public record TrackDeliveryResponseDto(
        String deliveryId,
        DeliveryStatus deliveryStatus,
        String trackingNumber,
        String packageName,
        BigDecimal packagePrice,
        DeliveryStatus status,
        String recipientName,
        String recipientPhone,
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
