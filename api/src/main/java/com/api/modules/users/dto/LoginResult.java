package com.api.modules.users.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {}
