package com.parrcel.api.modules.notification.dto;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationDto {
    private SubscriberDto subscriber;
    private String workflowIdentifier;
    private Map<String, Object> payload;
}