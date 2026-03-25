package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.dto.InitiatePaymentRequest;
import com.parrcel.api.modules.payment.providers.dto.ProviderInitResponse;

public interface PaymentProvider {

    String getProviderName();

    ProviderInitResponse initiateCollection(InitiatePaymentRequest request);

    default ProviderInitResponse initiateDisbursement(InitiatePaymentRequest request) {
        throw new UnsupportedOperationException(getProviderName() + " disbursement not yet implemented");
    }
}
