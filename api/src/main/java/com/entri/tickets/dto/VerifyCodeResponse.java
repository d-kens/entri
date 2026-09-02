package com.entri.tickets.dto;

public record VerifyCodeResponse(
        String eventExternalId,
        String eventTitle
) {}
