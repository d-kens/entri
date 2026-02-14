package com.parrcel.api.modules.payment.service;


import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.PaymentResponse;
import com.parrcel.api.modules.payment.providers.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final Map<String, PaymentProvider> paymentProviders;

    // Constructor injection - Spring auto-discovers all PaymentProvider beans
    public PaymentService(List<PaymentProvider> providers) {
        this.paymentProviders = providers.stream()
                .collect(Collectors.toMap(
                        PaymentProvider::getProviderName,
                        Function.identity()
                ));

        log.info("Registered payment providers: {}", paymentProviders.keySet());
    }

    public PaymentResponse initiatePayment(InitiatePaymentDto dto) {
        log.info("Routing payment for method: {}", dto.paymentMethod());

        PaymentProvider provider = paymentProviders.get(dto.paymentMethod());

        if (provider == null) {
            throw new NotFoundException(
                    "No provider found for payment method: " + dto.paymentMethod()
            );
        }

        return provider.initiatePayment(dto);
    }

}
