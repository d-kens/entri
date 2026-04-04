package com.oro.api.modules.payment.providers;

import com.oro.api.common.utils.PhoneNumberUtils;
import com.oro.api.modules.payment.dto.InitiatePaymentRequest;
import com.oro.api.modules.payment.exception.PaymentProviderException;
import com.oro.api.modules.payment.providers.client.MpesaClient;
import com.oro.api.modules.payment.providers.config.mpesa.MpesaProperties;
import com.oro.api.modules.payment.providers.dto.ProviderInitResponse;
import com.oro.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import com.oro.api.modules.payment.providers.dto.mpesa.MpesaStkRequestBody;
import com.oro.api.modules.payment.providers.dto.mpesa.MpesaStkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class Mpesa {

    private final MpesaClient mpesaClient;
    private final MpesaProperties mpesaProperties;

    public String authenticate() {
        String credentials = mpesaProperties.consumerKey() + ":" + mpesaProperties.consumerSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        log.info("Authenticating with Daraja API...");
        MpesaAuthResponse response = mpesaClient.authenticate("Basic " + encoded);
        log.info("Authenticated successfully. Token expires in: {}s", response.expiresIn());

        return response.accessToken();
    }

    public ProviderInitResponse initiateStkPush(InitiatePaymentRequest dto) {
        try {
            String token = "Bearer " + authenticate();
            String timestamp = generateTimestamp();
            String password = generatePassword(timestamp);
            String normalizedPhone = PhoneNumberUtils.normalize(dto.phoneNumber());

            MpesaStkRequestBody requestBody = new MpesaStkRequestBody(
                    mpesaProperties.shortcode(),
                    password,
                    timestamp,
                    "CustomerPayBillOnline",
                    String.valueOf(dto.amount()),
                    normalizedPhone,
                    mpesaProperties.shortcode(),
                    normalizedPhone,
                    mpesaProperties.callbackUrl(),
                    dto.paymentDescription(),
                    dto.paymentDescription()
            );

            log.info("Initiating STK Push for phone: {}, amount: {}", dto.phoneNumber(), dto.amount());
            MpesaStkResponse response = mpesaClient.stkPush(token, requestBody);
            log.info("STK Push response: {}", response.responseDescription());

            return new ProviderInitResponse(response.checkoutRequestId());
        } catch (PaymentProviderException e) {
            log.error("STK Push failed: {}", e.getMessage());
            throw e;
        }
    }

    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generatePassword(String timestamp) {
        String raw = mpesaProperties.shortcode() + mpesaProperties.passkey() + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}