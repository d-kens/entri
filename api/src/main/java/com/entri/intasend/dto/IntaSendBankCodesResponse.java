package com.entri.intasend.dto;

import java.util.List;

public record IntaSendBankCodesResponse(
        int count,
        List<IntaSendBankCode> results
) {
}
