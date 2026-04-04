package com.oro.api.modules.notification.events;

import com.oro.api.modules.notification.enums.NotificationType;
import com.oro.api.modules.user.entity.User;

import java.util.Map;

public record SendNotificationEvent(User user, Map<String, Object> payload, NotificationType notificationType) {}