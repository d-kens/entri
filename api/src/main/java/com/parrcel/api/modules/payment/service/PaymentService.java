package com.parrcel.api.modules.payment.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.payment.entity.Payment;
import com.parrcel.api.modules.payment.entity.PaymentStatus;
import com.parrcel.api.modules.payment.events.PaymentEvent;
import com.parrcel.api.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();


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

    public void sendPaymentEventToClients(String paymentId, PaymentEvent event) {
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
