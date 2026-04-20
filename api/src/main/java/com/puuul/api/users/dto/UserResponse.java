package com.puuul.api.users.dto;

public record UserResponse(
        String email,
        String externalKey,
        String firstName,
        String lastName,
        String phoneNumber
) {}
