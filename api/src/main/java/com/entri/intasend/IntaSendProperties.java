package com.entri.intasend;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "intasend")
public record IntaSendProperties(
        String baseUrl,
        String paymentBaseUrl,
        String publishableKey,
        String secretKey,
        String webhookChallenge
) {
}
