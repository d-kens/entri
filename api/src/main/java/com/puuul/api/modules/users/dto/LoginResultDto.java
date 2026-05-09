package com.puuul.api.modules.users.dto;

public record LoginResultDto(
        LoginResponseDto loginResponseDto,
        String refreshToken
) {}
