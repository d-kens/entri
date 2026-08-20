package com.entri.integrations.novu;

public record NovuSubscriber(
        String subscriberId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}
