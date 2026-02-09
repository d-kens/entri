package com.parrcel.api.modules.notification.events;

import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.users.model.User;

import java.util.Map;

public record SendNotificationEvent(User user, Map<String, Object> payload, NotificationType notificationType) {
}