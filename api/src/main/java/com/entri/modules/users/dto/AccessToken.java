package com.entri.modules.users.dto;

public record AccessToken(
        String token,
        long expiresIn
) {}
