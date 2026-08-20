package com.entri.users.dto;

public record AccessToken(
        String token,
        long expiresIn
) {}
