package com.entri.checkout.intasend;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "intasend")
public record IntaSendProperties(
        String baseUrl,
        String publishableKey,
        String secretKey
) {
}
