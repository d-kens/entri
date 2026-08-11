package com.entri.modules.payment.provider.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "intasend")
public record IntaSendProperties(
        String baseUrl,
        String publishableKey,
        String redirectUrl,
        String webhookUrl
) {
}
