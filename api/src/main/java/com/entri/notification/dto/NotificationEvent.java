package com.entri.notification.dto;

import com.entri.notification.NotificationType;

import java.util.Map;

public record NotificationEvent(
        NotificationType type,
        String recipientId,
        Map<String, Object> payload,
        NotificationRecipient recipient
) {
    public NotificationEvent(NotificationType type, String recipientId, Map<String, Object> payload) {
        this(type, recipientId, payload, null);
    }
}
