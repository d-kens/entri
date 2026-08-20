package com.entri.modules.notification.event;

import com.entri.modules.notification.WorkflowType;

import java.util.Map;

public record NotificationEvent(
        WorkflowType workflow,
        String subscriberId,
        Map<String, Object> payload
) {}
