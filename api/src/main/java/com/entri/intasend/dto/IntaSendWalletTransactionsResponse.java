package com.entri.intasend.dto;

import java.util.List;

public record IntaSendWalletTransactionsResponse(
        int count,
        String next,
        String previous,
        List<IntaSendWalletTransaction> results
) {
}
