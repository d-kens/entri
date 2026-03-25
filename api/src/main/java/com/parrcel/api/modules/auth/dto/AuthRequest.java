package com.parrcel.api.modules.auth.dto;

import com.parrcel.api.common.validators.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @ValidPhoneNumber
        String phoneNumber,
        @NotBlank(message = "password is required")
        String password
) {}