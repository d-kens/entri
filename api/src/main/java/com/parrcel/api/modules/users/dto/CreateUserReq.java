package com.parrcel.api.modules.users.dto;

import com.parrcel.api.common.validators.ValidPhoneNumber;
import com.parrcel.api.modules.users.validators.ValidRoles;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;


public record CreateUserReq(
        @NotBlank(message = "name is required")
        String name,

        @ValidPhoneNumber
        String phoneNumber,

        @ValidRoles
        Set<String> roles
) {}