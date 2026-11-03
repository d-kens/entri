package com.entri.auth.dto;

public record RefreshResult(
        AccessToken accessToken,
        String refreshToken,
        int refreshTokenMaxAgeSeconds
) {}
