package com.puuul.api.users.dto;

public record LoginResponseDto(
        String role,
        String firstName,
        String lastName,
        String accessToken
) {}
