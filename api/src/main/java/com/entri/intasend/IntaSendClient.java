package com.entri.intasend;

import com.entri.exception.PaymentGatewayException;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
import com.entri.intasend.dto.IntaSendCheckoutResponse;
import com.entri.intasend.dto.IntaSendWalletRequest;
import com.entri.intasend.dto.IntaSendWalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class IntaSendClient {

    private final RestClient intaSendRestClient;
    private final IntaSendProperties properties;

    public IntaSendCheckoutResponse createCheckout(IntaSendCheckoutRequest request) {
        try {
            return intaSendRestClient.post()
                    .uri("/api/v1/checkout/")
                    .body(request)
                    .retrieve()
                    .body(IntaSendCheckoutResponse.class);
        } catch (RestClientException e) {
            throw new PaymentGatewayException("IntaSend checkout failed: " + e.getMessage(), e);
        }
    }

    public IntaSendWalletResponse createWallet(IntaSendWalletRequest request) {
        try {
            return intaSendRestClient.post()
                    .uri("/api/v1/wallets/")
                    .header("Authorization", "Bearer " + properties.secretKey())
                    .body(request)
                    .retrieve()
                    .body(IntaSendWalletResponse.class);
        } catch (RestClientException e) {
            throw new PaymentGatewayException("IntaSend wallet creation failed: " + e.getMessage(), e);
        }
    }

    public IntaSendWalletResponse getWallet(String walletId) {
        try {
            return intaSendRestClient.get()
                    .uri("/api/v1/wallets/{walletId}/", walletId)
                    .header("Authorization", "Bearer " + properties.secretKey())
                    .retrieve()
                    .body(IntaSendWalletResponse.class);
        } catch (RestClientException e) {
            throw new PaymentGatewayException("IntaSend wallet fetch failed: " + e.getMessage(), e);
        }
    }
}
