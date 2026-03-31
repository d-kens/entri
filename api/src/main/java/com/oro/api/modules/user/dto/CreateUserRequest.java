package com.oro.api.modules.user.dto;

import com.oro.api.common.validators.ValidPhoneNumber;
import com.oro.api.modules.user.validators.ValidRoles;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;


public record CreateUserRequest(
        @NotBlank(message = "name is required")
        String name,

        @ValidPhoneNumber
        String phoneNumber,

        @ValidRoles
        Set<String> roles
) {}