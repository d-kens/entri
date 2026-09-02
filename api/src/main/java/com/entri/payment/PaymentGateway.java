package com.entri.payment;

import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.dto.PaymentResult;
import com.entri.payment.dto.WebhookRequest;

import java.util.Optional;

public interface PaymentGateway {
    String checkout(CheckoutRequest request);
    Optional<PaymentResult> parseCheckoutWebhook(WebhookRequest webhookRequest);
}
