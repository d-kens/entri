package com.oro.api.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cookie")
@Data
public class CookieConfig {
    private boolean secure = true;
}
