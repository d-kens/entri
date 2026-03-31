package com.oro.api.modules.notification.entity;

import lombok.Getter;

@Getter
public enum NotificationType {

    OTP("OTP"),
    DELIVERY_FEE_PAYMENT("DELIVERY_FEE_PAYMENT");

    private final String notificationType;

    NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}