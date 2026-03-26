package com.parrcel.api.modules.payment.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentRequest;
import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.entity.Payment;
import com.parrcel.api.modules.payment.entity.PaymentMethod;
import com.parrcel.api.modules.payment.entity.PaymentStatus;
import com.parrcel.api.modules.payment.events.PaymentEvent;
import com.parrcel.api.modules.payment.providers.Mpesa;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallback;
import com.parrcel.api.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final Mpesa mpesa;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {

        Payment payment = Payment.builder()
                .referenceId(request.referenceId())
                .amount(request.amount())
                .description(request.paymentDescription())
                .method(PaymentMethod.valueOf(request.paymentMethod()))
                .phoneNumber(request.phoneNumber())
                .build();

        payment = paymentRepository.save(payment);

        ProviderInitResponse providerResponse = mpesa.initiateStkPush(request);

        payment.setProviderTransactionId(providerResponse.providerTransactionId());
        paymentRepository.save(payment);

        log.info("{} Payment {} created, awaiting callback for providerTransactionId: {}",
                payment.getExternalId(), providerResponse.providerTransactionId());

        return new InitiatePaymentResponse(payment.getExternalId(), payment.getReferenceId());
    }


    public void handleStkCallback(MpesaStkCallback mpesaStkCallback) {
        MpesaStkCallback.StkCallback stk = mpesaStkCallback.body().stkCallback();
        boolean success = stk.resultCode() == 0;
        String providerReference = success ? extractMetadataValue(stk, "MpesaReceiptNumber") : null;
        String failureReason = success ? null : stk.resultCode() + " - " + stk.resultDesc();
        BigDecimal amount = success ? extractAmount(stk) : null;
        LocalDateTime transactionDate = success ? extractTransactionDate(stk) : null;

        processPaymentStatus(
                stk.checkoutRequestId(),
                success,
                providerReference,
                failureReason,
                amount,
                transactionDate
        );
    }

    private void processPaymentStatus(String providerTransactionId, boolean success, String providerReference, String failureReason, BigDecimal amount, LocalDateTime transactionDate) {
        Payment payment = findPaymentByProviderTransactionId(providerTransactionId);

        if (payment.isTerminal()) {
            log.warn("Payment {} is already {} — ignoring duplicate update",
                    payment.getExternalId(), payment.getStatus());
            return;
        }

        PaymentEvent event;
        if (success) {
            markPaymentAsPaid(payment, providerReference);
            event = PaymentEvent.success(
                    payment.getExternalId(),
                    payment.getReferenceId(),
                    amount,
                    providerReference,
                    transactionDate
            );
        } else {
            markPaymentAsFailed(payment, failureReason);
            event = PaymentEvent.failure(
                    payment.getExternalId(),
                    payment.getReferenceId(),
                    failureReason
            );
        }

        eventPublisher.publishEvent(event);
        sendPaymentEventToClients(payment.getExternalId(), event);
    }

    private String extractMetadataValue(MpesaStkCallback.StkCallback stk, String name) {
        if (stk.callbackMetadata() == null || stk.callbackMetadata().item() == null) {
            return null;
        }
        return stk.callbackMetadata().item().stream()
                .filter(item -> name.equals(item.name()))
                .map(item -> item.value() != null ? item.value().toString() : null)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal extractAmount(MpesaStkCallback.StkCallback stk) {
        String value = extractMetadataValue(stk, "Amount");
        return value != null ? new BigDecimal(value) : null;
    }

    private LocalDateTime extractTransactionDate(MpesaStkCallback.StkCallback stk) {
        String value = extractMetadataValue(stk, "TransactionDate");
        if (value == null) return null;
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public Payment findPaymentByProviderTransactionId(String providerTransactionId) {
        return  paymentRepository.findByProviderTransactionId(providerTransactionId).orElseThrow(
                () -> new NotFoundException("Payment with provider transaction ID: " + providerTransactionId + " not found")
        );
    }

    public void markPaymentAsPaid(Payment payment, String receiptNumber) {
        payment.markPaid(receiptNumber);
        paymentRepository.save(payment);
    }
    public void markPaymentAsFailed(Payment payment, String reason) {
        payment.markFailed(reason);
        paymentRepository.save(payment);
    }

    public SseEmitter streamPaymentEvents(String paymentId) {
        SseEmitter emitter = new SseEmitter(240_000L);

        sseEmitters.computeIfAbsent(paymentId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        log.info("Client subscribed to payment events for: {}. Active connections: {}",
                paymentId, sseEmitters.get(paymentId).size());

        emitter.onCompletion(() -> {
            removeSseEmitter(paymentId, emitter);
            log.info("SSE connection completed for payment: {}", paymentId);
        });

        emitter.onTimeout(() -> {
            removeSseEmitter(paymentId, emitter);
            log.warn("SSE connection timed out for payment: {}", paymentId);
        });

        emitter.onError((ex) -> {
            removeSseEmitter(paymentId, emitter);
            log.error("SSE connection error for payment: {}", paymentId, ex);
        });

        return emitter;
    }

    private void sendPaymentEventToClients(String paymentId, PaymentEvent event) {
        CopyOnWriteArrayList<SseEmitter> paymentEmitters = sseEmitters.get(paymentId);

        if (paymentEmitters == null || paymentEmitters.isEmpty()) {
            log.debug("No active SSE connections for payment: {}", paymentId);
            return;
        }

        log.info("Sending payment event to {} client(s) for payment: {}",
                paymentEmitters.size(), paymentId);

        for (SseEmitter emitter : paymentEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("payment-status")
                        .data(event));

                log.debug("Successfully sent event to client for payment: {}", paymentId);

                if (event.status() == PaymentStatus.SUCCESS || event.status() == PaymentStatus.FAILED) {
                    emitter.complete();
                }
            } catch (IOException e) {
                log.error("Failed to send SSE event for payment: {}", paymentId, e);
                removeSseEmitter(paymentId, emitter);
            }
        }

        if (event.status() == PaymentStatus.SUCCESS || event.status() == PaymentStatus.FAILED) {
            sseEmitters.remove(paymentId);
            log.info("Cleaned up emitters for completed payment: {}", paymentId);
        }
    }

    private void removeSseEmitter(String paymentId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> paymentEmitters = sseEmitters.get(paymentId);

        if (paymentEmitters != null) {
            paymentEmitters.remove(emitter);

            if (paymentEmitters.isEmpty()) {
                sseEmitters.remove(paymentId);
                log.info("All emitters removed for payment: {}", paymentId);
            }
        }
    }
}
