package com.entri.payment;

import com.entri.exception.PaymentGatewayException;
import com.entri.payment.dto.WebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher paymentEventPublisher;

    public void handleWebhook(final Map<String, String> headers, final String payload) {
        var webhookRequest = new WebhookRequest(headers, payload);

        Optional<PaymentResultEvent> checkoutEvent = Optional.empty();
        Optional<PayoutResultEvent> payoutEvent = Optional.empty();

        try {
            checkoutEvent = paymentGateway.parseCheckoutWebhook(webhookRequest)
                    .map(r -> new PaymentResultEvent(r.referenceId(), r.status()));
        } catch (PaymentGatewayException e) {
            log.warn("Checkout webhook processing failed: {}", e.getMessage());
        }

        try {
            payoutEvent = paymentGateway.parsePayoutWebhook(webhookRequest)
                    .map(r -> new PayoutResultEvent(r.trackingId(), r.status()));
        } catch (PaymentGatewayException e) {
            log.warn("Payout webhook processing failed: {}", e.getMessage());
        }

        checkoutEvent.ifPresent(paymentEventPublisher::publishWebhookResult);
        payoutEvent.ifPresent(paymentEventPublisher::publishPayoutResult);

        if (checkoutEvent.isEmpty() && payoutEvent.isEmpty()) {
            log.info("Unrecognized webhook payload: {}",
                    payload.substring(0, Math.min(payload.length(), 200)));
        }
    }
}
