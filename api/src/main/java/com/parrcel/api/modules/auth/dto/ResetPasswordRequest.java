package com.parrcel.api.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "token is required")
        String token,

        @NotBlank(message = "new password is required")
        @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
        String newPassword
) {}