package com.parrcel.api.modules.payment.service;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.entity.PaymentDirection;
import com.parrcel.api.modules.payment.entity.PaymentMethod;
import com.parrcel.api.modules.payment.entity.PaymentStatus;
import com.parrcel.api.modules.payment.events.PaymentEvent;
import com.parrcel.api.modules.payment.entity.Payment;
import com.parrcel.api.modules.payment.providers.Mpesa;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaParseCallbackResult;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallbackDto;
import com.parrcel.api.modules.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
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
    public InitiatePaymentResponse initiatePayment(InitiatePaymentDto initiatePaymentDto) {
        Payment payment = Payment.builder()
                .paymentType(initiatePaymentDto.paymentType())
                .referenceId(initiatePaymentDto.referenceId())
                .amount(initiatePaymentDto.amount())
                .description(initiatePaymentDto.paymentDescription())
                .method(PaymentMethod.valueOf(initiatePaymentDto.paymentMethod()))
                .phoneNumber(initiatePaymentDto.phoneNumber())
                .paymentDirection(PaymentDirection.INBOUND)
                .build();

        payment = paymentRepository.save(payment);

        ProviderInitResponse providerInitResponse = mpesa.initiatePayment(initiatePaymentDto);

        payment.setProviderTransactionId(providerInitResponse.providerTransactionId());
        paymentRepository.save(payment);

        log.info("Payment {} created, awaiting callback for providerTransactionId: {}",
                payment.getExternalId(), providerInitResponse.providerTransactionId());

        return new InitiatePaymentResponse(
                payment.getExternalId(),
                payment.getReferenceId()
        );
    }
    @Transactional
    public void handleMpesaCallback(MpesaStkCallbackDto dto) {
        MpesaParseCallbackResult result = mpesa.parseCallback(dto);

        Optional<Payment> optionalPayment = paymentRepository
                .findByProviderTransactionId(result.providerTransactionId());

        if (optionalPayment.isEmpty()) {
            log.warn("Received callback for unknown providerTransactionId: {} — ignoring",
                    result.providerTransactionId());
            return;
        }

        Payment payment = optionalPayment.get();

        if (payment.isTerminal()) {
            log.warn("Payment {} is already {} — ignoring duplicate callback",
                    payment.getExternalId(), payment.getStatus());
            return;
        }

        PaymentEvent event;

        if (result.success()) {
            payment.markPaid(result.providerReference());
            paymentRepository.save(payment);
            log.info("Payment {} SUCCESSFUL — ref: {}", payment.getExternalId(), result.providerReference());

            event = PaymentEvent.success(
                    payment.getExternalId(),
                    payment.getPaymentType(),
                    payment.getReferenceId(),
                    result.amount(),
                    result.providerReference(),
                    result.transactionDate()
            );

        } else {
            payment.markFailed(result.failureReason());
            paymentRepository.save(payment);
            log.warn("Payment {} FAILED — reason: {}", payment.getExternalId(), result.failureReason());

            event = PaymentEvent.failure(
                    payment.getExternalId(),
                    payment.getPaymentType(),
                    payment.getReferenceId(),
                    result.failureReason()
            );
        }

        eventPublisher.publishEvent(event);

        sendPaymentEventToClients(payment.getExternalId(), event);
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

                // Close emitter after sending terminal event
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
