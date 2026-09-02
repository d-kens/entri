package com.entri.intasend.dto;

import java.math.BigDecimal;

public record IntaSendSendMoneyResponse(
        String id,
        String status,
        String currency,
        BigDecimal total
) {
}
