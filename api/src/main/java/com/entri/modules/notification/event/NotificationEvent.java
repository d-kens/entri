package com.entri.modules.notification.event;

import com.entri.client.novu.WorkflowType;

import java.util.Map;

public record NotificationEvent(
        WorkflowType workflow,
        String subscriberId,
        Map<String, Object> payload
) {}
