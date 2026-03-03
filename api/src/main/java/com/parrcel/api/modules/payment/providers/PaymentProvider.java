package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.providers.dto.ProviderCallbackResult;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;

public interface PaymentProvider {
    String authenticate();
    String getProviderName();
    ProviderCallbackResult parseCallback(Object rawCallback);
    ProviderInitResponse initiatePayment(InitiatePaymentDto dto);

}
