package com.oro.api.modules.notification.dto;

import java.util.Map;

public record NotificationDto(
        SubscriberDto subscriber,
        String workflowIdentifier,
        Map<String, Object> payload
) {}