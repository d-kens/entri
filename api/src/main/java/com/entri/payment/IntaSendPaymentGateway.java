package com.entri.payment;

import com.entri.exception.PaymentGatewayException;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.IntaSendProperties;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
import com.entri.intasend.dto.IntaSendWebhookPayload;
import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.dto.PaymentResult;
import com.entri.payment.dto.WebhookRequest;

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
