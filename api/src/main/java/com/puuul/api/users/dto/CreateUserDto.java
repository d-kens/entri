package com.puuul.api.users.dto;

import com.puuul.api.common.validators.ValidPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name os required")
        String lastName,
        @ValidPhoneNumber
        String phoneNumber
) {}
