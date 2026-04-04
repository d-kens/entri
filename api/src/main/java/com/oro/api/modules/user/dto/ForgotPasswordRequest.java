package com.oro.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank String phoneNumber
) {}
