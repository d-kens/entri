package com.entri.modules.payment.provider.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class IntaSendConfiguration {

    @Bean
    public RestClient intaSendRestClient(
            final IntaSendProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(
                        "X-IntaSend-Public-API-Key",
                        properties.publishableKey()
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }
}
