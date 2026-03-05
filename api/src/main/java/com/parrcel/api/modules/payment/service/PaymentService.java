package com.parrcel.api.modules.payment.service;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.enums.PaymentDirection;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.payment.events.PaymentFailedEvent;
import com.parrcel.api.modules.payment.events.PaymentSuccessEvent;
import com.parrcel.api.modules.payment.model.Payment;
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

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final Mpesa mpesa;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        if (result.success()) {
            payment.markPaid(result.providerReference());
            paymentRepository.save(payment);
            log.info("Payment {} SUCCESSFUL — ref: {}", payment.getExternalId(), result.providerReference());

            eventPublisher.publishEvent(new PaymentSuccessEvent(
                    payment.getExternalId(),
                    payment.getPaymentType(),
                    payment.getReferenceId(),
                    result.amount(),
                    result.transactionDate()
            ));


        } else {
            payment.markFailed(result.failureReason());
            paymentRepository.save(payment);
            log.warn("Payment {} FAILED — reason: {}", payment.getExternalId(), result.failureReason());

            eventPublisher.publishEvent(new PaymentFailedEvent(
                    payment.getExternalId(),
                    payment.getPaymentType(),
                    payment.getReferenceId(),
                    result.failureReason()
            ));
        }
    }
}
