package com.parrcel.api.modules.payment.providers.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mpesa")
public record MpesaProperties(
        String baseUrl,
        String consumerKey,
        String consumerSecret,
        String shortcode,
        String passkey,
        String callbackUrl
) {}
