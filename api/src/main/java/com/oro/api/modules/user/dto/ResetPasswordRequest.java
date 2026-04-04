package com.oro.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 6, max = 6) String otp,
        @NotBlank @Size(min = 8) String newPassword
) {}
