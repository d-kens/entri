package com.entri.payouts.dto;

import com.entri.payouts.entity.PayoutMethod;

public record PayoutAccountResponse(
        String externalId,
        PayoutMethod method,
        String recipientName,
        String account,
        String accountReference,
        String bankCode,
        boolean isDefault
) {
}
