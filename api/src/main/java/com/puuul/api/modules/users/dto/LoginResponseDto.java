package com.puuul.api.modules.users.dto;

public record LoginResponseDto(
        UserResponseDto user,
        AccessTokenDto accessToken
) {}
