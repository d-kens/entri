package com.entri.payment;

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

    /**
     * Verification failures (bad challenge, status lookup failed or disagrees) propagate as
     * PaymentGatewayException → 502, so a forged webhook is rejected and a genuine one that
     * couldn't be verified (e.g. IntaSend API briefly down) is retried by IntaSend rather than dropped.
     */
    public void handleWebhook(final Map<String, String> headers, final String payload) {
        var webhookRequest = new WebhookRequest(headers, payload);

        Optional<PaymentResultEvent> checkoutEvent = paymentGateway.parseCheckoutWebhook(webhookRequest)
                .map(r -> new PaymentResultEvent(r.referenceId(), r.status(), r.amount(), r.currency()));

        Optional<PayoutResultEvent> payoutEvent = paymentGateway.parsePayoutWebhook(webhookRequest)
                .map(r -> new PayoutResultEvent(r.trackingId(), r.status()));

        checkoutEvent.ifPresent(paymentEventPublisher::publishWebhookResult);
        payoutEvent.ifPresent(paymentEventPublisher::publishPayoutResult);

        if (checkoutEvent.isEmpty() && payoutEvent.isEmpty()) {
            log.info("Unrecognized webhook payload: {}",
                    payload.substring(0, Math.min(payload.length(), 200)));
        }
    }
}
