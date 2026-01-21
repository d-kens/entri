package com.parrcel.api.modules.token.dto;

import com.parrcel.api.modules.token.enums.TokenPurpose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
public class TokenResponseDto {
    private String rawToken;
    private TokenPurpose purpose;
    private Instant expiresAt;
}