package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.exception.PaymentProviderException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.providers.client.MpesaClient;
import com.parrcel.api.modules.payment.providers.config.mpesa.MpesaProperties;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaParseCallbackResult;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallbackDto;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkRequestBody;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkResponse;
import com.parrcel.api.modules.payment.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public ProviderInitResponse initiatePayment(InitiatePaymentDto dto) {
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
            log.info("STK Push response: {}", response);
            log.info("STK Push response: {}", response.responseDescription());

            return new ProviderInitResponse(response.checkoutRequestId());
        } catch (PaymentProviderException e) {
            log.error("STK Push failed: {}", e.getMessage());
            throw e;
        }
    }

    public MpesaParseCallbackResult parseCallback(MpesaStkCallbackDto dto) {
        MpesaStkCallbackDto.StkCallback stk = dto.body().stkCallback();

        log.info("Parsing M-Pesa callback — CheckoutRequestID: {}, ResultCode: {}",
                stk.checkoutRequestId(), stk.resultCode());

        boolean success = stk.resultCode() == 0;

        if (!success) {
            return new MpesaParseCallbackResult(
                    false,
                    stk.checkoutRequestId(),
                    null,
                    null,
                    null,
                    null,
                    stk.resultCode() + " - " + stk.resultDesc()
            );
        }

        return new MpesaParseCallbackResult(
                true,
                stk.checkoutRequestId(),
                extractMetadataValue(stk, "MpesaReceiptNumber"),
                extractAmount(stk),
                extractTransactionDate(stk),
                extractPhoneNumber(stk),
                null
        );
    }

    private String extractMetadataValue(MpesaStkCallbackDto.StkCallback stk, String name) {
        if (stk.callbackMetadata() == null || stk.callbackMetadata().item() == null) {
            return null;
        }

        return stk.callbackMetadata().item().stream()
                .filter(item -> name.equals(item.name()))
                .map(item -> item.value() != null ? item.value().toString() : null)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal extractAmount(MpesaStkCallbackDto.StkCallback stk) {
        String value = extractMetadataValue(stk, "Amount");
        return value != null ? new BigDecimal(value) : null;
    }

    private LocalDateTime extractTransactionDate(MpesaStkCallbackDto.StkCallback stk) {
        String value = extractMetadataValue(stk, "TransactionDate");
        if (value == null) return null;
        // Safaricom format: yyyyMMddHHmmss e.g. 20191219102115
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String extractPhoneNumber(MpesaStkCallbackDto.StkCallback stk) {
        String value = extractMetadataValue(stk, "PhoneNumber");
        // Safaricom returns as long e.g. 254708374149
        return value != null ? "+" + value : null;
    }
}