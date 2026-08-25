package com.entri.checkout.intasend;

import com.entri.checkout.PaymentGateway;
import com.entri.checkout.dto.CheckoutResponse;
import com.entri.checkout.dto.PaymentResult;
import com.entri.checkout.dto.WebhookRequest;
import com.entri.checkout.intasend.dto.IntaSendCheckoutRequest;
import com.entri.checkout.intasend.dto.IntaSendCheckoutResponse;
import com.entri.events.entity.EventTicketReservation;
import com.entri.exception.PaymentGatewayException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class IntaSendPaymentGateway implements PaymentGateway {

    @Value("${payment.redirect-url}")
    private String paymentRedirectUrl;

    private final RestClient restClient;
    private final IntaSendProperties intaSendProperties;

    @Override
    public CheckoutResponse checkout(EventTicketReservation eventTicketReservation) {
        var intaSendCheckoutRequest = new IntaSendCheckoutRequest(
                eventTicketReservation.getFirstName(),
                eventTicketReservation.getLastName(),
                eventTicketReservation.getPhoneNumber(),
                eventTicketReservation.getEmail(),
                eventTicketReservation.getExternalId(),
                "WEBSITE",
                paymentRedirectUrl,
                eventTicketReservation.getTotalAmount(),
                eventTicketReservation.getEvent().getCurrency()
        );

        try {
            var response = restClient.post()
                    .uri("/api/v1/checkout/")
                    .body(intaSendCheckoutRequest)
                    .retrieve()
                    .body(IntaSendCheckoutResponse.class);

            return new CheckoutResponse(response.url());
        } catch (RestClientException exception) {
            throw new PaymentGatewayException("IntaSend checkout failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest webhookRequest) {
        return Optional.empty();
    }
}
