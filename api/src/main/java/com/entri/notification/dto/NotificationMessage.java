package com.entri.notification.dto;

import com.entri.notification.NotificationType;

import java.util.Map;

public record NotificationMessage(
        NotificationType type,
        String subscriberId,
        Map<String, Object> payload
) {
}
