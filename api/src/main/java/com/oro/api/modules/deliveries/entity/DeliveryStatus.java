package com.oro.api.modules.deliveries.entity;

public enum DeliveryStatus {
    PENDING,
    AT_PICKUP_AGENT,
    AT_HUB,
    OUT_FOR_DELIVERY,
    AT_DESTINATION_AGENT,
    DELIVERED,
    CANCELLED;

    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean canTransitionTo(DeliveryStatus status) {
        return switch(this) {
            case PENDING -> status == AT_PICKUP_AGENT || status == CANCELLED;
            case AT_PICKUP_AGENT -> status == AT_HUB || status == CANCELLED;
            case AT_HUB -> status == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> status == AT_DESTINATION_AGENT;
            case AT_DESTINATION_AGENT -> status == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public boolean isAgentAllowed() {
        return this == AT_PICKUP_AGENT || this == DELIVERED;
    }
}