package com.entri.wallet.dto;

import com.entri.wallet.WalletTransactionStatus;

public record WithdrawalResponse(
        String externalId,
        WalletTransactionStatus status
) {
}
