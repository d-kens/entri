package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.common.exception.PaymentProviderException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.PaymentResponse;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.payment.providers.client.MpesaClient;
import com.parrcel.api.modules.payment.providers.config.mpesa.MpesaProperties;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkRequestBody;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkResponse;
import com.parrcel.api.modules.payment.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class Mpesa implements PaymentProvider {
    private final MpesaClient mpesaClient;
    private final MpesaProperties mpesaProperties;


    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generatePassword(String timestamp) {
        String raw = mpesaProperties.shortcode() + mpesaProperties.passkey() + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }


    public String authenticate() {
        String credentials = mpesaProperties.consumerKey() + ":" + mpesaProperties.consumerSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        log.info("Authenticating with Daraja API...");
        MpesaAuthResponse response = mpesaClient.authenticate("Basic " + encoded);
        log.info("Authenticated successfully. Token expires in: {}s", response.expiresIn());

        return response.accessToken();
    }

    @Override
    public PaymentResponse initiatePayment(InitiatePaymentDto dto) {
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
                    dto.reference(),
                    "Payment for " + dto.reference()
            );

            log.info("Initiating STK Push for phone: {}, amount: {}", dto.phoneNumber(), dto.amount());
            MpesaStkResponse response = mpesaClient.stkPush(token, requestBody);
            log.info("STK Push response: {}", response);
            log.info("STK Push response: {}", response.responseDescription());

            return null;
        } catch (PaymentProviderException e) {
            log.error("STK Push failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String getProviderName() {
        return PaymentMethod.MPESA.toString();
    }
}
