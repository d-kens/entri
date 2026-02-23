package com.parrcel.api.modules.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record ChangePasswordReq (
        @NotBlank(message = "current password is required")
        String currentPassword,

        @NotBlank(message = "new password is required")
        @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
        String newPassword
) {}