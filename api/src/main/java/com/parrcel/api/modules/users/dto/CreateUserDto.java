package com.parrcel.api.modules.users.dto;

import com.parrcel.api.modules.users.validation.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateUserDto(
        @NotBlank(message = "username is required")
        String userName,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
        String password,

        @NotBlank(message = "phone number is required")
        @Size(min = 10, max = 15, message = "phone number should be between 10 and 15 characters")
        String phoneNumber,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email")
        String email,

        @ValidRole
        String role
) {}