package com.parrcel.api.modules.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    WELCOME_NOTIFICATION("WELCOME_NOTIFICATION"),
    RESET_PASSWORD("RESET_PASSWORD"),
    PASSWORD_RESET_CONFIRMATION("PASSWORD_RESET_CONFIRMATION");

    private final String notificationType;

    NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}