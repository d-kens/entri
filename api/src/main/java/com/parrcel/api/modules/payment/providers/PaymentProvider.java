package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import com.parrcel.api.modules.payment.dto.PaymentResponse;

public interface PaymentProvider {
    PaymentResponse initiatePayment(InitiatePaymentDto dto);
    String getProviderName();
}
