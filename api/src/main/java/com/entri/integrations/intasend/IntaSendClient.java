package com.entri.integrations.intasend;

import com.entri.shared.exception.PaymentProviderException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IntaSendClient {
    private final RestClient restClient;
    private final IntaSendProperties properties;

    public IntaSendClient(final RestClient intaSendRestClient, final IntaSendProperties properties) {
        this.restClient = intaSendRestClient;
        this.properties = properties;
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

    public IntaSendWalletResponse createWallet(final IntaSendWalletRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/wallets/")
                    .header("Authorization", "Bearer " + properties.secretKey())
                    .body(request)
                    .retrieve()
                    .body(IntaSendWalletResponse.class);
        } catch (RestClientException e) {
            throw new PaymentProviderException("IntaSend wallet creation failed: " + e.getMessage(), e);
        }
    }

    public IntaSendWalletResponse getWallet(final String walletId) {
        try {
            return restClient.get()
                    .uri("/api/v1/wallets/{walletId}/", walletId)
                    .header("Authorization", "Bearer " + properties.secretKey())
                    .retrieve()
                    .body(IntaSendWalletResponse.class);
        } catch (RestClientException e) {
            throw new PaymentProviderException("IntaSend wallet fetch failed: " + e.getMessage(), e);
        }
    }
}
