package com.parrcel.api.modules.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TokenValidationDto(
        @NotBlank(message = "Token cannot be blank")
        @JsonProperty("token")
        String token
) {}
