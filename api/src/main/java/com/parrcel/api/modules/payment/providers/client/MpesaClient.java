package com.parrcel.api.modules.payment.providers.client;

import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkRequestBody;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "mpesa-client",
        url = "${mpesa.base-url}"
)
public interface MpesaClient {
    @GetMapping("${mpesa.endpoints.auth}")
    MpesaAuthResponse authenticate(@RequestHeader("Authorization") String authorization);

    @PostMapping("${mpesa.endpoints.stk-push}")
    MpesaStkResponse stkPush(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MpesaStkRequestBody body
    );
}
