package com.entri.users.dto;

import com.entri.shared.validators.ValidPhoneNumber;
import com.entri.users.validators.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
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
        String phoneNumber,

        @ValidRole
        String role
) {}
