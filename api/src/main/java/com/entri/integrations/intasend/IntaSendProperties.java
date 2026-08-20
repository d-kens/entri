package com.entri.integrations.intasend;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "intasend")
public record IntaSendProperties(
        String baseUrl,
        String publishableKey
) {
}
