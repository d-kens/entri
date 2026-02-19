package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.PaymentResponse;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.payment.providers.client.MpesaClient;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class Mpesa implements PaymentProvider {
    @org.springframework.beans.factory.annotation.Value("${mpesa.consumer-key}")
    private String consumerKey;

    @org.springframework.beans.factory.annotation.Value("${mpesa.consumer-secret}")
    private String consumerSecret;


    private final MpesaClient mpesaClient;

    public String authenticate() {
        String credentials = consumerKey + ":" + consumerSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        log.info("Authenticating with Daraja API...");
        MpesaAuthResponse response = mpesaClient.authenticate("Basic " + encoded);
        log.info("Authenticated successfully. Token expires in: {}s", response.expiresIn());

        return response.accessToken();
    }

    @Override
    public PaymentResponse initiatePayment(InitiatePaymentDto dto) {
        String accessToken = authenticate();
        return null;
    }

    @Override
    public String getProviderName() {
        return PaymentMethod.MPESA.toString();
    }
}
