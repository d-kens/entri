package com.entri.tickets.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
        @NotBlank String checkInCode
) {}
