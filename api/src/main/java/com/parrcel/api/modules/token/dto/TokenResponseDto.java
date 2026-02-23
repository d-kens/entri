package com.parrcel.api.modules.token.dto;

import com.parrcel.api.modules.token.enums.TokenPurpose;

import java.time.Instant;

public record TokenResponseDto(
        String rawToken,
        TokenPurpose purpose,
        Instant expiresAt
) {}