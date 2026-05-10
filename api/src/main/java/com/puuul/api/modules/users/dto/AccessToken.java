package com.puuul.api.modules.users.dto;

public record AccessToken(
        String token,
        long expiresIn
) {}
