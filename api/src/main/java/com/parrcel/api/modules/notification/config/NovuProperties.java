package com.parrcel.api.modules.notification.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "novu")
public class NovuProperties {
    private String apiKey;
}