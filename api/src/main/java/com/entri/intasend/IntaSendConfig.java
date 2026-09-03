package com.entri.intasend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class IntaSendConfig {

    @Bean
    public RestClient intaSendRestClient(
            final IntaSendProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-IntaSend-Public-API-Key", properties.publishableKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient intaSendSendMoneyRestClient(
            final IntaSendProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Token " + properties.secretKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
