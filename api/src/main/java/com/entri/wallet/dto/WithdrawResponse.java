package com.entri.wallet.dto;

public record WithdrawResponse(
        String trackingId,
        String status
) {
}
