package com.oro.api.modules.routes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignDriverDto(
        @NotBlank @Size(max = 100) String driverName,
        @NotBlank @Size(max = 15) String driverPhone
) {}
