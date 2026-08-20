package com.entri.users.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {}
