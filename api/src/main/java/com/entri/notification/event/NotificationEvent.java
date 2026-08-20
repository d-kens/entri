package com.entri.notification.event;

import com.entri.integrations.novu.WorkflowType;

import java.util.Map;

public record NotificationEvent(
        WorkflowType workflow,
        String subscriberId,
        Map<String, Object> payload
) {}
