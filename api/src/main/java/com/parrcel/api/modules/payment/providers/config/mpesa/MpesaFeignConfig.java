package com.parrcel.api.modules.payment.providers.config.mpesa;


import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MpesaFeignConfig {
    @Bean
    public ErrorDecoder mpesaErrorDecoder() {
        return new MpesaErrorDecoder();
    }
}
