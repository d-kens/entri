package com.entri.modules.users.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {}
