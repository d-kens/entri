package com.puuul.api.modules.notification.event;

import com.puuul.api.client.novu.WorkflowType;

import java.util.Map;

public record NotificationEvent(
        WorkflowType workflow,
        String subscriberId,
        Map<String, Object> payload
) {}
