package com.api.modules.users.dto;

public record AccessToken(
        String token,
        long expiresIn
) {}
