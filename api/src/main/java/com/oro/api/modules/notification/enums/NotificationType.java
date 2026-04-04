package com.oro.api.modules.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    RESET_PASSWORD("RESET_PASSWORD"),
    DELIVERY_FEE_PAYMENT("DELIVERY_FEE_PAYMENT");

    private final String notificationType;

    NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}