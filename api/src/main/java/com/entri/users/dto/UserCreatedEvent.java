package com.entri.users.dto;

public record UserCreatedEvent(
        String externalKey,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String role
) {
}
