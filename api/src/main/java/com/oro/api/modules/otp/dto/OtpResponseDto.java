package com.oro.api.modules.otp.dto;

public record OtpResponseDto(
        String otp,
        long expiresInMinutes
) {}
