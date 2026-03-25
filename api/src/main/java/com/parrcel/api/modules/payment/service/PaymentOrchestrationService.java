package com.parrcel.api.modules.payment.service;

import com.parrcel.api.modules.payment.dto.InitiatePaymentRequest;
import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.entity.Payment;
import com.parrcel.api.modules.payment.entity.PaymentDirection;
import com.parrcel.api.modules.payment.entity.PaymentMethod;
import com.parrcel.api.modules.payment.providers.PaymentProvider;
import com.parrcel.api.modules.payment.providers.PaymentProviderRegistry;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestrationService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderRegistry providerRegistry;

    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        Payment payment = Payment.builder()
                .paymentType(request.paymentType())
                .referenceId(request.referenceId())
                .amount(request.amount())
                .description(request.paymentDescription())
                .method(PaymentMethod.valueOf(request.paymentMethod()))
                .phoneNumber(request.phoneNumber())
                .paymentDirection(PaymentDirection.INBOUND)
                .build();

        payment = paymentRepository.save(payment);

        PaymentProvider provider = providerRegistry.getProvider(request.paymentMethod());
        ProviderInitResponse providerResponse = provider.initiateCollection(request);

        payment.setProviderTransactionId(providerResponse.providerTransactionId());
        paymentRepository.save(payment);

        log.info("Payment {} created, awaiting callback for providerTransactionId: {}",
                payment.getExternalId(), providerResponse.providerTransactionId());

        return new InitiatePaymentResponse(payment.getExternalId(), payment.getReferenceId());
    }
}
