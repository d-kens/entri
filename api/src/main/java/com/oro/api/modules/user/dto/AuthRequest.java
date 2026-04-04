package com.oro.api.modules.user.dto;

import com.oro.api.common.validators.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @ValidPhoneNumber
        String phoneNumber,

        @NotBlank(message = "password is required")
        String password
) {}