package com.entri.checkout;

import com.entri.checkout.dto.CheckoutResponse;
import com.entri.checkout.dto.PaymentResult;
import com.entri.checkout.dto.WebhookRequest;
import com.entri.checkout.enums.PaymentStatus;
import com.entri.events.entity.EventTicketReservation;
import com.entri.exception.PaymentGatewayException;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.IntaSendProperties;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
import com.entri.intasend.dto.IntaSendWebhookPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class IntaSendPaymentGateway implements PaymentGateway {

    @Value("${payment.redirect-url}")
    private String paymentRedirectUrl;

    private final IntaSendClient intaSendClient;
    private final IntaSendProperties intaSendProperties;
    private final ObjectMapper objectMapper;

    @Override
    public CheckoutResponse checkout(EventTicketReservation eventTicketReservation) {
        var request = new IntaSendCheckoutRequest(
                eventTicketReservation.getFirstName(),
                eventTicketReservation.getLastName(),
                eventTicketReservation.getPhoneNumber(),
                eventTicketReservation.getEmail(),
                eventTicketReservation.getExternalId(),
                "WEBSITE",
                paymentRedirectUrl,
                eventTicketReservation.getTotalAmount(),
                eventTicketReservation.getEvent().getCurrency(),
                eventTicketReservation.getEvent().getOrganizer().getWalletId()
        );

        var response = intaSendClient.createCheckout(request);
        return new CheckoutResponse(response.url());
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest webhookRequest) {
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
