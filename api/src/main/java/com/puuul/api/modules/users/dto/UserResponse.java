package com.puuul.api.modules.users.dto;

public record UserResponse(
        String role,
        String email,
        String lastName,
        String firstName,
        String phoneNumber,
        String externalKey
) {}
