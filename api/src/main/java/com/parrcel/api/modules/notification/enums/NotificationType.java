package com.parrcel.api.modules.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    RESET_PASSWORD("reset_password"),
    WELCOME_EMAIL("welcome_email");

    private final String notificationType;

    NotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

}