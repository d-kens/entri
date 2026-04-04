package com.oro.api.modules.user.dto;

import com.oro.api.common.validators.ValidPhoneNumber;
import com.oro.api.modules.user.validators.ValidRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "name is required")
        String name,

        @ValidPhoneNumber
        String phoneNumber,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password,

        @ValidRole(excluded = {"MERCHANT"})
        String role
) {}
