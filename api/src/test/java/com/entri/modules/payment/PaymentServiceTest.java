package com.entri.modules.payment;

import com.entri.exception.PaymentGatewayException;
import com.entri.payment.PaymentEventPublisher;
import com.entri.payment.PaymentGateway;
import com.entri.payment.PaymentResultEvent;
import com.entri.payment.PaymentService;
import com.entri.payment.PayoutResult;
import com.entri.payment.PayoutResultEvent;
import com.entri.payment.dto.PaymentResult;
import com.entri.payment.dto.WebhookRequest;
import com.entri.payment.enums.PaymentStatus;
import com.entri.payment.enums.PayoutStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentGateway paymentGateway;
    @Mock PaymentEventPublisher paymentEventPublisher;

    @InjectMocks PaymentService paymentService;

    @Test
    void handleWebhook_checkoutWebhook_publishesPaymentResultEvent() {
        var headers = Map.of("x-signature", "abc");
        when(paymentGateway.parseCheckoutWebhook(new WebhookRequest(headers, "payload")))
                .thenReturn(Optional.of(new PaymentResult("ref-1", PaymentStatus.PAID)));
        when(paymentGateway.parsePayoutWebhook(any())).thenReturn(Optional.empty());

        paymentService.handleWebhook(headers, "payload");

        verify(paymentEventPublisher).publishWebhookResult(new PaymentResultEvent("ref-1", PaymentStatus.PAID));
        verify(paymentEventPublisher, never()).publishPayoutResult(any());
    }

    @Test
    void handleWebhook_payoutWebhook_publishesPayoutResultEvent() {
        var headers = Map.of("x-signature", "abc");
        when(paymentGateway.parseCheckoutWebhook(any())).thenReturn(Optional.empty());
        when(paymentGateway.parsePayoutWebhook(new WebhookRequest(headers, "payload")))
                .thenReturn(Optional.of(new PayoutResult("track-1", PayoutStatus.COMPLETED)));

        paymentService.handleWebhook(headers, "payload");

        verify(paymentEventPublisher).publishPayoutResult(new PayoutResultEvent("track-1", PayoutStatus.COMPLETED));
        verify(paymentEventPublisher, never()).publishWebhookResult(any());
    }

    @Test
    void handleWebhook_neitherCheckoutNorPayout_publishesNothing() {
        when(paymentGateway.parseCheckoutWebhook(any())).thenReturn(Optional.empty());
        when(paymentGateway.parsePayoutWebhook(any())).thenReturn(Optional.empty());

        paymentService.handleWebhook(Map.of(), "unrecognized-payload");

        verify(paymentEventPublisher, never()).publishWebhookResult(any());
        verify(paymentEventPublisher, never()).publishPayoutResult(any());
    }

    @Test
    void handleWebhook_checkoutParsingThrows_stillChecksPayoutAndSwallowsException() {
        when(paymentGateway.parseCheckoutWebhook(any())).thenThrow(new PaymentGatewayException("bad signature"));
        when(paymentGateway.parsePayoutWebhook(any())).thenReturn(Optional.of(new PayoutResult("track-1", PayoutStatus.FAILED)));

        paymentService.handleWebhook(Map.of(), "payload");

        verify(paymentEventPublisher).publishPayoutResult(new PayoutResultEvent("track-1", PayoutStatus.FAILED));
    }

    @Test
    void handleWebhook_bothParsersThrow_doesNotPropagateException() {
        when(paymentGateway.parseCheckoutWebhook(any())).thenThrow(new PaymentGatewayException("bad checkout"));
        when(paymentGateway.parsePayoutWebhook(any())).thenThrow(new PaymentGatewayException("bad payout"));

        paymentService.handleWebhook(Map.of(), "payload");

        verify(paymentEventPublisher, never()).publishWebhookResult(any());
        verify(paymentEventPublisher, never()).publishPayoutResult(any());
    }
}
