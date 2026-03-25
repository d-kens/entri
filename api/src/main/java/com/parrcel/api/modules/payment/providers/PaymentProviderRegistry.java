package com.parrcel.api.modules.payment.providers;

import com.parrcel.api.modules.payment.exception.PaymentProviderException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProviderRegistry {

    private final Map<String, PaymentProvider> providers;

    public PaymentProviderRegistry(List<PaymentProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(PaymentProvider::getProviderName, Function.identity()));
    }

    public PaymentProvider getProvider(String providerName) {
        PaymentProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new PaymentProviderException("No payment provider registered for: " + providerName);
        }
        return provider;
    }
}
