package com.parrcel.api.modules.auth.dto;

public record TokenPair (
        String accessToken,
        String refreshToken

) {}