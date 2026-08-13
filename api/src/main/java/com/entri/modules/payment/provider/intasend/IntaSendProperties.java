package com.entri.modules.payment.provider.intasend;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "intasend")
public record IntaSendProperties(
        String baseUrl,
        String publishableKey,
        String redirectUrl
) {
}
