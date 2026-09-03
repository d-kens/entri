package com.entri.users.dto;

public record UserUpdatedEvent(
        String externalKey,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
