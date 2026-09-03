package com.entri.payment;

import com.entri.exception.PaymentGatewayException;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.IntaSendProperties;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
import com.entri.intasend.dto.IntaSendPayoutWebhookPayload;
import com.entri.intasend.dto.IntaSendSendMoneyRequest;
import com.entri.intasend.dto.IntaSendSendMoneyTransaction;
import com.entri.intasend.dto.IntaSendWebhookPayload;
import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.dto.PaymentResult;
import com.entri.payment.dto.PayoutRequest;
import com.entri.payment.dto.WebhookRequest;

import com.entri.payment.enums.AccountType;
import com.entri.payment.enums.PaymentStatus;
import com.entri.payment.enums.PayoutStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IntaSendPaymentGateway implements PaymentGateway {

    private final IntaSendClient intaSendClient;
    private final IntaSendProperties intaSendProperties;
    private final ObjectMapper objectMapper;

    private static final String CURRENCY = "KES";
    private static final String COUNTRY = "KE";

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.api-url}")
    private String appApiUrl;

    @Override
    public String checkout(CheckoutRequest request) {
        var intaSendRequest = new IntaSendCheckoutRequest(
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.email(),
                request.reference(),
                "WEBSITE",
                request.redirectUrl(),
                request.amount(),
                request.currency(),
                null,
                "CUSTOMER-PAYS",
                "CUSTOMER-PAYS"
        );

        return intaSendClient.createCheckout(intaSendRequest).url();
    }

    @Override
    public String payout(PayoutRequest request) {
        var transaction = new IntaSendSendMoneyTransaction(
                request.name(),
                request.account(),
                toIntaSendAccountType(request.accountType()),
                request.accountReference(),
                request.bankCode(),
                request.amount().toPlainString(),
                request.narrative()
        );

        var callbackUrl = appApiUrl.stripTrailing() + "/api/payment/webhook";

        var sendMoneyRequest = new IntaSendSendMoneyRequest(
                request.currency(),
                toIntaSendProvider(request.accountType()),
                COUNTRY,
                "NO",
                callbackUrl,
                List.of(transaction)
        );

        return intaSendClient.sendMoney(sendMoneyRequest).trackingId();
    }

    @Override
    public Optional<PaymentResult> parseCheckoutWebhook(WebhookRequest webhookRequest) {
        JsonNode root;
        try {
            root = objectMapper.readTree(webhookRequest.payload());
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to parse IntaSend webhook payload", e);
        }

        if (!root.has("api_ref")) {
            return Optional.empty();
        }

        IntaSendWebhookPayload payload;
        try {
            payload = objectMapper.treeToValue(root, IntaSendWebhookPayload.class);
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to parse IntaSend checkout webhook payload", e);
        }

        if (!intaSendProperties.webhookChallenge().equals(payload.challenge())) {
            throw new PaymentGatewayException("Invalid IntaSend webhook challenge");
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

    @Override
    public Optional<PayoutResult> parsePayoutWebhook(WebhookRequest webhookRequest) {
        JsonNode root;
        try {
            root = objectMapper.readTree(webhookRequest.payload());
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to parse IntaSend webhook payload", e);
        }

        if (!root.has("tracking_id")) {
            return Optional.empty();
        }

        IntaSendPayoutWebhookPayload payload;
        try {
            payload = objectMapper.treeToValue(root, IntaSendPayoutWebhookPayload.class);
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to parse IntaSend payout webhook payload", e);
        }

        if (!intaSendProperties.webhookChallenge().equals(payload.challenge())) {
            throw new PaymentGatewayException("Invalid IntaSend payout webhook challenge");
        }

        // Each withdrawal sends exactly one transaction. If batched payouts are added, this mapping must be revisited.
        PayoutStatus status = switch (payload.status()) {
            case "Completed" -> {
                boolean allSuccessful = payload.transactions().stream()
                        .allMatch(t -> "Successful".equals(t.status()));
                yield allSuccessful ? PayoutStatus.COMPLETED : PayoutStatus.FAILED;
            }
            case "Failed Processing" -> PayoutStatus.FAILED;
            default -> null;
        };

        if (status == null) {
            return Optional.empty();
        }

        return Optional.of(new PayoutResult(payload.trackingId(), status));
    }

    private String toIntaSendAccountType(AccountType type) {
        // BANK returns null — IntaSendSendMoneyTransaction is @JsonInclude(NON_NULL) so the field is omitted for bank payouts
        return switch (type) {
            case PAYBILL -> "PayBill";
            case TILL_NUMBER -> "TillNumber";
            case BANK -> null;
        };
    }

    private String toIntaSendProvider(AccountType type) {
        return switch (type) {
            case PAYBILL, TILL_NUMBER -> "MPESA-B2B";
            case BANK -> "PESALINK";
        };
    }
}
