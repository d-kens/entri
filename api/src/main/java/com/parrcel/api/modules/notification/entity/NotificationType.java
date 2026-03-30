package com.parrcel.api.modules.notification.entity;

import lombok.Getter;

@Getter
public enum NotificationType {

    RESET_PASSWORD("RESET_PASSWORD"),
    DELIVERY_FEE_PAYMENT("DELIVERY_FEE_PAYMENT"),
    WELCOME_NOTIFICATION("WELCOME_NOTIFICATION"),
    PASSWORD_RESET_CONFIRMATION("PASSWORD_RESET_CONFIRMATION");

    private final String notificationType;

    NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}