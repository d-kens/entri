package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.PaymentResponse;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Wallet implements PaymentProvider{
    @Override
    public PaymentResponse initiatePayment(InitiatePaymentDto dto) {
        return null;
    }

    @Override
    public String getProviderName() {
        return PaymentMethod.WALLET.toString();
    }
}
