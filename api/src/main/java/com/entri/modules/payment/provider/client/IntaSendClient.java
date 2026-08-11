package com.entri.modules.payment.provider.client;


import com.entri.modules.payment.provider.client.intasend.dto.IntaSendCheckoutRequest;
import com.entri.modules.payment.provider.client.intasend.dto.IntaSendCheckoutResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IntaSendClient {
    private final RestClient restClient;

    public IntaSendClient(final RestClient intaSendRestClient) {
        this.restClient = intaSendRestClient;
    }

    public IntaSendCheckoutResponse createCheckout(
            final IntaSendCheckoutRequest request
    ) {
        return restClient.post()
                .uri("/api/v1/checkout/")
                .body(request)
                .retrieve()
                .body(IntaSendCheckoutResponse.class);
    }
}
