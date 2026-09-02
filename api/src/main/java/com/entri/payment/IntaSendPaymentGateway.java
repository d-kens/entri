package com.entri.payment;

import com.entri.exception.PaymentGatewayException;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.IntaSendProperties;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
import com.entri.intasend.dto.IntaSendSendMoneyRequest;
import com.entri.intasend.dto.IntaSendSendMoneyWebhookPayload;
import com.entri.intasend.dto.IntaSendTransactionItem;
import com.entri.intasend.dto.IntaSendWebhookPayload;
import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.dto.PaymentResult;
import com.entri.payment.dto.WebhookRequest;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class IntaSendPaymentGateway implements PaymentGateway {

    private final IntaSendClient intaSendClient;
    private final IntaSendProperties intaSendProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String checkout(CheckoutRequest request) {
        var intaSendRequest = new IntaSendCheckoutRequest(
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.email(),
                request.reference(),
                "WEBSITE",
                request.redirectUrl(),
                request.amount(),
                request.currency(),
                null,
                "CUSTOMER-PAYS",
                "CUSTOMER-PAYS"
        );

        return intaSendClient.createCheckout(intaSendRequest).url();
    }

    @Override
    public String sendPayout(PayoutRequest request) {
        var item = new IntaSendTransactionItem(
                request.recipientName(),
                request.account(),
                request.accountReference(),
                request.bankCode(),
                request.amount(),
                "Organizer payout " + request.idempotencyKey()
        );

        String uri = switch (request.method()) {
            case MPESA_PAYBILL, MPESA_TILL -> "/api/v1/send-money/mpesa/";
            case BANK -> "/api/v1/send-money/bank/";
        };

        return intaSendClient.sendMoney(uri,
                new IntaSendSendMoneyRequest(request.currency(), List.of(item), null, "NO"))
                .trackingId();
    }

    @Override
    public Optional<PayoutResult> parsePayoutWebhook(WebhookRequest webhookRequest) {
        IntaSendSendMoneyWebhookPayload payload;
        try {
            payload = objectMapper.readValue(webhookRequest.payload(), IntaSendSendMoneyWebhookPayload.class);
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to parse IntaSend send-money webhook payload", e);
        }

        String challenge = payload.challenge();
        if (challenge != null && !intaSendProperties.webhookChallenge().equals(challenge)) {
            throw new PaymentGatewayException("Invalid IntaSend webhook challenge", null);
        }

        if (payload.trackingId() == null) {
            return Optional.empty();
        }

        boolean allSuccessful = payload.transactions() != null
                && payload.transactions().stream().allMatch(t -> "Successful".equals(t.status()));

        return Optional.of(new PayoutResult(
                payload.trackingId(),
                "Completed".equals(payload.status()) && allSuccessful
        ));
    }

    @Override
    public Optional<PaymentResult> parseCheckoutWebhook(WebhookRequest webhookRequest) {
        IntaSendWebhookPayload payload;
        try {
            payload = objectMapper.readValue(webhookRequest.payload(), IntaSendWebhookPayload.class);
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to parse IntaSend webhook payload", e);
        }

        if (!intaSendProperties.webhookChallenge().equals(payload.challenge())) {
            throw new PaymentGatewayException("Invalid IntaSend webhook challenge", null);
        }

        PaymentStatus status = switch (payload.state()) {
            case "COMPLETE" -> PaymentStatus.PAID;
            case "FAILED" -> PaymentStatus.FAILED;
            default -> null;
        };

        if (status == null) {
            return Optional.empty();
        }

        return Optional.of(new PaymentResult(payload.apiRef(), status));
    }
}
