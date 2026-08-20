package com.entri.users.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Password reset token is required")
        String token,
        @NotBlank(message = "Password is required")
        String password
) {
}
