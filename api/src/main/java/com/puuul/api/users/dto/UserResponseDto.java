package com.puuul.api.users.dto;

public record UserResponseDto(
        String role,
        String email,
        String lastName,
        String firstName,
        String phoneNumber,
        String externalKey
) {}
