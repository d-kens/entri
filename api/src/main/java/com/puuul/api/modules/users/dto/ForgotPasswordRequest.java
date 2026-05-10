package com.puuul.api.modules.users.dto;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
        @Email(message = "Email must be valid")
        String email
) {}
