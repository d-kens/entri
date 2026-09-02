package com.entri.payouts.dto;

import com.entri.payouts.entity.PayoutMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PayoutAccountRequest(
        @NotNull PayoutMethod method,
        @NotBlank String recipientName,
        @NotBlank String account,
        String accountReference,
        String bankCode,
        boolean isDefault
) {
}
