package com.entri.config;

import co.novu.Novu;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NovuConfig {

    @Value("${novu.api.key}")
    private String apiKey;

    @Bean
    public Novu novu() {
        return Novu.builder().secretKey(apiKey).build();
    }
}
