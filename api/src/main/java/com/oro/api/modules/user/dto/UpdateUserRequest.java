package com.oro.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "name is required")
        String name
) {}
