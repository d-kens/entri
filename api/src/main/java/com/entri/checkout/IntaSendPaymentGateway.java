package com.entri.checkout;

import com.entri.checkout.dto.CheckoutResponse;
import com.entri.checkout.dto.PaymentResult;
import com.entri.checkout.dto.WebhookRequest;
import com.entri.events.entity.EventTicketReservation;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
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
        return Optional.empty();
    }
}
