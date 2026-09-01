package com.entri.users.dto;

public record UserResponse(
        String role,
        String email,
        String lastName,
        String firstName,
        String phoneNumber,
        String externalKey,
        boolean enabled
) {}
