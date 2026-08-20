package com.entri.integrations.intasend;

import com.entri.shared.exception.PaymentProviderException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IntaSendClient {
    private final RestClient restClient;

    public IntaSendClient(final RestClient intaSendRestClient) {
        this.restClient = intaSendRestClient;
    }

    public IntaSendCheckoutResponse createCheckout(
            final IntaSendCheckoutRequest request
    ) {
        try {
            return restClient.post()
                    .uri("/api/v1/checkout/")
                    .body(request)
                    .retrieve()
                    .body(IntaSendCheckoutResponse.class);
        } catch (RestClientException e) {
            throw new PaymentProviderException("IntaSend checkout failed: " + e.getMessage(), e);
        }
    }
}
