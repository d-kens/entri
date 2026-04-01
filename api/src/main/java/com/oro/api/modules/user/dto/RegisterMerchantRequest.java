package com.oro.api.modules.user.dto;

import com.oro.api.common.validators.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;

public record RegisterMerchantRequest(
        @NotBlank(message = "name is required")
        String name,

        @ValidPhoneNumber
        String phoneNumber
) {}
