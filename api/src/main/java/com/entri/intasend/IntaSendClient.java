package com.entri.intasend;

import com.entri.exception.PaymentGatewayException;
import com.entri.intasend.dto.IntaSendCheckoutRequest;
import com.entri.intasend.dto.IntaSendCheckoutResponse;
import com.entri.intasend.dto.IntaSendSendMoneyRequest;
import com.entri.intasend.dto.IntaSendSendMoneyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class IntaSendClient {

    private final RestClient intaSendRestClient;

    @Qualifier("intaSendSendMoneyRestClient")
    private final RestClient intaSendSendMoneyRestClient;

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

    public IntaSendSendMoneyResponse sendMoney(IntaSendSendMoneyRequest request) {
        try {
            return intaSendSendMoneyRestClient.post()
                    .uri("/api/v1/send-money/initiate/")
                    .body(request)
                    .retrieve()
                    .body(IntaSendSendMoneyResponse.class);
        } catch (RestClientException e) {
            throw new PaymentGatewayException("IntaSend send money failed: " + e.getMessage(), e);
        }
    }
}
