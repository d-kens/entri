package com.parrcel.api.modules.payment.providers.client;

import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "mpesa-client",
        url = "${mpesa.base-url}"
)
public interface MpesaClient {
    @GetMapping("/oauth/v1/generate?grant_type=client_credentials")
    MpesaAuthResponse authenticate(@RequestHeader("Authorization") String authorization);
}
