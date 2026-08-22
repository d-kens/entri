package com.entri.auth.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {}
