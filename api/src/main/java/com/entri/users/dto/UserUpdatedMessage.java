package com.entri.users.dto;

public record UserUpdatedMessage(
        String externalKey,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
