package com.parrcel.api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "token is required")
    private String token;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email")
    private String newPassword;
}