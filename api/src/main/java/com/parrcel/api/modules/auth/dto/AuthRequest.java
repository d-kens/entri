package com.parrcel.api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "email is required")
    @Email(message = "email should be a valid email")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}