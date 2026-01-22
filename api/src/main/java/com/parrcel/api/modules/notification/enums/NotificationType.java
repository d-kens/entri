package com.parrcel.api.modules.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    RESET_PASSWORD("RESET_PASSWORD"),
    WELCOME_EMAIL("WELCOME_EMAIL");

    private final String notificationType;

    NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

}