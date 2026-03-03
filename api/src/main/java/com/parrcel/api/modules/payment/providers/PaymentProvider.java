package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;

public interface PaymentProvider {
    String authenticate();
    ProviderInitResponse initiatePayment(InitiatePaymentDto dto);
    String getProviderName();
}
