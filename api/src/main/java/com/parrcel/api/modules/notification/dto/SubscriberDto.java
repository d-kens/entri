package com.parrcel.api.modules.notification.dto;

public record SubscriberDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber
) {}