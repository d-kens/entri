package com.parrcel.api.modules.deliveries.enums;

public enum DeliveryStatus {
    PENDING,
    DROPPED_AT_PICKUP_AGENT,
    AT_HUB,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean canTransitionTo(DeliveryStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == DROPPED_AT_PICKUP_AGENT || newStatus == CANCELLED;
            case DROPPED_AT_PICKUP_AGENT -> newStatus == AT_HUB || newStatus == CANCELLED;
            case AT_HUB -> newStatus == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}