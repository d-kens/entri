package com.parrcel.api.modules.notification.events;

import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.user.entity.User;
import lombok.Getter;

import java.util.Map;

@Getter
public class NotificationEvent {
    private final User user;
    private final Map<String, Object> payload;
    private final NotificationType notificationType;

    public NotificationEvent(User user, Map<String, Object> payload,  NotificationType notificationType) {
        this.user = user;
        this.payload = payload;
        this.notificationType = notificationType;
    }
}