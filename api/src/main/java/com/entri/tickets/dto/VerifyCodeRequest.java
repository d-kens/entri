package com.entri.tickets.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank String code
) {}
