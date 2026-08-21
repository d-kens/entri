package com.entri.integrations.intasend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntaSendWalletRequest(

        @JsonProperty("wallet_type")
        String walletType,

        String currency,

        String label,

        @JsonProperty("can_disburse")
        boolean canDisburse
) {
}
