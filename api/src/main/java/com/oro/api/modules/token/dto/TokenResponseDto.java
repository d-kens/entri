package com.oro.api.modules.token.dto;

import com.oro.api.modules.token.entity.TokenPurpose;
import java.time.Instant;

public record TokenResponseDto(
        String rawToken,
        TokenPurpose purpose,
        Instant expiresAt
) {}