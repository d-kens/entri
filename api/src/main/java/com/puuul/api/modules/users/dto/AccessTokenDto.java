package com.puuul.api.modules.users.dto;

public record AccessTokenDto(
        String token,
        long expiresIn
) {}
