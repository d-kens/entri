package com.entri.checkout;


import com.entri.checkout.dto.CheckoutResponse;
import com.entri.checkout.dto.PaymentResult;
import com.entri.checkout.dto.WebhookRequest;
import com.entri.events.entity.EventTicketReservation;

import java.util.Optional;

public interface PaymentGateway {
    CheckoutResponse checkout(EventTicketReservation eventTicketReservation);
    Optional<PaymentResult> parseWebhookRequest(WebhookRequest webhookRequest);
}
