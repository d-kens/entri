package com.parrcel.api.modules.payment.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.payment.model.Payment;
import com.parrcel.api.modules.payment.providers.PaymentProvider;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentProvider> paymentProviders;

    public PaymentService(
            List<PaymentProvider> providers,
            PaymentRepository paymentRepository
    ) {
        this.paymentProviders = providers.stream()
                .collect(Collectors.toMap(
                        PaymentProvider::getProviderName,
                        Function.identity()
                ));

        this.paymentRepository = paymentRepository;

        log.info("Registered payment providers: {}", paymentProviders.keySet());
    }


    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentDto initiatePaymentDto) {
        log.info("Routing payment for method: {}", initiatePaymentDto.paymentMethod());

        PaymentProvider provider = paymentProviders.get(initiatePaymentDto.paymentMethod());

        if (provider == null) {
            throw new NotFoundException(
                    "No provider found for payment method: " + initiatePaymentDto.paymentMethod()
            );
        }

        Payment payment = Payment.builder()
                .payableType(initiatePaymentDto.payableType())
                .payableId(initiatePaymentDto.payableId())
                .amount(initiatePaymentDto.amount())
                .description(initiatePaymentDto.paymentDescription())
                .method(PaymentMethod.valueOf(initiatePaymentDto.paymentMethod()))
                .phoneNumber(initiatePaymentDto.phoneNumber())
                .build();

        payment = paymentRepository.save(payment);

        ProviderInitResponse providerInitResponse = provider.initiatePayment(initiatePaymentDto);

        payment.setProviderTransactionId(providerInitResponse.providerTransactionId());

        log.info("Payment {} created, awaiting callback for providerTransactionId: {}",
                payment.getExternalId(), providerInitResponse.providerTransactionId());

        return new InitiatePaymentResponse(
                payment.getExternalId(),
                payment.getPayableId()
        );
    }

}
