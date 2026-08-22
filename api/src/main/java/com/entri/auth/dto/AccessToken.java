package com.entri.auth.dto;

public record AccessToken(
        String token,
        long expiresIn
) {}
