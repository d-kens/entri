package com.entri.checkout;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(BigDecimal serviceFeeRate) {
}
