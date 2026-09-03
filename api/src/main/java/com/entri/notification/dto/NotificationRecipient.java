package com.entri.notification.dto;

public record NotificationRecipient(
        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}
