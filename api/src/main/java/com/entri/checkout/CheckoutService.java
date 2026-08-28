package com.entri.checkout;

import com.entri.checkout.dto.CheckoutRequest;
import com.entri.checkout.dto.CheckoutResponse;
import com.entri.checkout.dto.PaymentResultMessage;
import com.entri.checkout.dto.WebhookRequest;
import com.entri.events.service.EventTicketReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final EventTicketReservationService eventTicketReservationService;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    CheckoutResponse checkout(final String reservationId, final CheckoutRequest checkoutRequest) {
        var eventTicketReservation = eventTicketReservationService.getPendingReservation(reservationId);

        eventTicketReservation.setFirstName(checkoutRequest.firstName());
        eventTicketReservation.setLastName(checkoutRequest.lastName());
        eventTicketReservation.setEmail(checkoutRequest.email());
        eventTicketReservation.setPhoneNumber(checkoutRequest.phoneNumber());

        return paymentGateway.checkout(eventTicketReservation);
    }

    void handleWebhook(final Map<String, String> headers, final String payload) {
        var webhookRequest = new WebhookRequest(headers, payload);
        paymentGateway.parseWebhookRequest(webhookRequest)
                .map(result -> new PaymentResultMessage(result.reservationExternalId(), result.status()))
                .ifPresent(paymentEventPublisher::publishWebhookResult);
    }
}
