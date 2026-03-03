package com.parrcel.api.modules.payment.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.payment.model.Payment;
import com.parrcel.api.modules.payment.providers.PaymentProvider;
import com.parrcel.api.modules.payment.providers.dto.ProviderCallbackResult;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallbackDto;
import com.parrcel.api.modules.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        paymentRepository.save(payment);

        log.info("Payment {} created, awaiting callback for providerTransactionId: {}",
                payment.getExternalId(), providerInitResponse.providerTransactionId());

        return new InitiatePaymentResponse(
                payment.getExternalId(),
                payment.getPayableId()
        );
    }


    @Transactional
    public void handleMpesaCallback(MpesaStkCallbackDto dto) {
        PaymentProvider mpesaProvider = paymentProviders.get(PaymentMethod.MPESA.toString());
        ProviderCallbackResult result = mpesaProvider.parseCallback(dto);

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
            log.info("Payment {} PAID — ref: {}", payment.getExternalId(), result.providerReference());
        } else {
            payment.markFailed(result.failureReason());
            paymentRepository.save(payment);
            log.warn("Payment {} FAILED — reason: {}", payment.getExternalId(), result.failureReason());
        }
    }
}
