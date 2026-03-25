package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentRequest;
import com.parrcel.api.modules.payment.events.PaymentEvent;
import com.parrcel.api.modules.payment.exception.PaymentProviderException;
import com.parrcel.api.modules.payment.providers.client.MpesaClient;
import com.parrcel.api.modules.payment.providers.config.mpesa.MpesaProperties;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaAuthResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallback;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkRequestBody;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkResponse;
import com.parrcel.api.modules.payment.service.PaymentService;
import com.parrcel.api.modules.payment.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaProvider implements PaymentProvider {

    private final MpesaClient mpesaClient;
    private final MpesaProperties mpesaProperties;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;


    public String authenticate() {
        String credentials = mpesaProperties.consumerKey() + ":" + mpesaProperties.consumerSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        log.info("Authenticating with Daraja API...");
        MpesaAuthResponse response = mpesaClient.authenticate("Basic " + encoded);
        log.info("Authenticated successfully. Token expires in: {}s", response.expiresIn());

        return response.accessToken();
    }

    @Override
    public String getProviderName() {
        return "MPESA";
    }

    @Override
    public ProviderInitResponse initiateCollection(InitiatePaymentRequest dto) {
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

    @Override
    public ProviderInitResponse initiateDisbursement(InitiatePaymentRequest request) {
        log.warn("M-Pesa B2C disbursement is not yet implemented");
        throw new UnsupportedOperationException("M-Pesa B2C disbursement not yet implemented");
    }

    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generatePassword(String timestamp) {
        String raw = mpesaProperties.shortcode() + mpesaProperties.passkey() + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public void handleStkCallback(MpesaStkCallback mpesaStkCallback) {
        MpesaStkCallback.StkCallback stk = mpesaStkCallback.body().stkCallback();
        try {
            var payment = paymentService.findPaymentByProviderTransactionId(stk.checkoutRequestId());

            if (payment.isTerminal()) {
                log.warn("Payment {} is already {} — ignoring duplicate callback",
                        payment.getExternalId(), payment.getStatus());
                return;
            }

            PaymentEvent event;

            if (stk.resultCode() == 0) {
                String receiptNumber = extractMetadataValue(stk, "MpesaReceiptNumber");
                paymentService.markPaymentAsPaid(payment, receiptNumber);

                event = PaymentEvent.success(
                        payment.getExternalId(),
                        payment.getPaymentType(),
                        payment.getReferenceId(),
                        extractAmount(stk),
                        receiptNumber,
                        extractTransactionDate(stk)
                );
            } else {
                String failureReason = stk.resultCode() + " - " + stk.resultDesc();
                paymentService.markPaymentAsFailed(payment, failureReason);

                event = PaymentEvent.failure(
                        payment.getExternalId(),
                        payment.getPaymentType(),
                        payment.getReferenceId(),
                        failureReason
                );
            }

            eventPublisher.publishEvent(event);
            paymentService.sendPaymentEventToClients(payment.getExternalId(), event);

        } catch (NotFoundException e) {
            log.warn("Received callback for unknown providerTransactionId: {} — ignoring",
                    stk.checkoutRequestId());
        }
    }

    private String extractMetadataValue(MpesaStkCallback.StkCallback stk, String name) {
        if (stk.callbackMetadata() == null || stk.callbackMetadata().item() == null) {
            return null;
        }
        return stk.callbackMetadata().item().stream()
                .filter(item -> name.equals(item.name()))
                .map(item -> item.value() != null ? item.value().toString() : null)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal extractAmount(MpesaStkCallback.StkCallback stk) {
        String value = extractMetadataValue(stk, "Amount");
        return value != null ? new BigDecimal(value) : null;
    }

    private LocalDateTime extractTransactionDate(MpesaStkCallback.StkCallback stk) {
        String value = extractMetadataValue(stk, "TransactionDate");
        if (value == null) return null;
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}