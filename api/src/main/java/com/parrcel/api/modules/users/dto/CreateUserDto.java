package com.parrcel.api.modules.users.dto;

import com.parrcel.api.common.validators.ValidPhoneNumber;
import com.parrcel.api.modules.users.validators.ValidRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateUserDto(
        @NotBlank(message = "username is required")
        String userName,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
        String password,

        @ValidPhoneNumber
        String phoneNumber,

        String email,

        @ValidRole
        String role
) {}