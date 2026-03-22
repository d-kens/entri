package com.parrcel.api.modules.wallet.dto;

import com.parrcel.api.modules.wallet.entity.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        String externalId,
        BigDecimal balance,
        WalletStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
