package com.parrcel.api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email should be a valid email")
        String email,
        @NotBlank(message = "password is required")
        String password
) {}