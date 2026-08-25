package com.entri.wallet.intasend;

import com.entri.checkout.intasend.IntaSendProperties;
import com.entri.exception.PaymentGatewayException;
import com.entri.wallet.WalletProvider;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.intasend.dto.IntaSendWalletRequest;
import com.entri.wallet.intasend.dto.IntaSendWalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class IntaSendWalletProvider implements WalletProvider {

    private final RestClient intaSendRestClient;
    private final IntaSendProperties intaSendProperties;

    @Override
    public String createWallet(final String label, final String currency) {
        var request = new IntaSendWalletRequest("SETTLEMENT", currency, label, true);
        try {
            var response = intaSendRestClient.post()
                    .uri("/api/v1/wallets/")
                    .header("Authorization", "Bearer " + intaSendProperties.secretKey())
                    .body(request)
                    .retrieve()
                    .body(IntaSendWalletResponse.class);
            return response.walletId();
        } catch (RestClientException e) {
            throw new PaymentGatewayException("IntaSend wallet creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public WalletResponse getWallet(final String walletId) {
        try {
            var response = intaSendRestClient.get()
                    .uri("/api/v1/wallets/{walletId}/", walletId)
                    .header("Authorization", "Bearer " + intaSendProperties.secretKey())
                    .retrieve()
                    .body(IntaSendWalletResponse.class);
            return new WalletResponse(
                    response.walletId(),
                    response.label(),
                    response.currency(),
                    response.currentBalance(),
                    response.availableBalance()
            );
        } catch (RestClientException e) {
            throw new PaymentGatewayException("IntaSend wallet fetch failed: " + e.getMessage(), e);
        }
    }
}
