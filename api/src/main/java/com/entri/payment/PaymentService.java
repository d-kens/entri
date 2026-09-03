package com.entri.payment;

import com.entri.payment.dto.WebhookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher paymentEventPublisher;

    public void handleWebhook(final Map<String, String> headers, final String payload) {
        var webhookRequest = new WebhookRequest(headers, payload);

        paymentGateway.parseCheckoutWebhook(webhookRequest)
                .map(result -> new PaymentResultEvent(result.referenceId(), result.status()))
                .ifPresent(paymentEventPublisher::publishWebhookResult);

        paymentGateway.parsePayoutWebhook(webhookRequest)
                .map(result -> new PayoutResultEvent(result.trackingId(), result.status()))
                .ifPresent(paymentEventPublisher::publishPayoutResult);
    }
}
