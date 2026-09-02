package com.entri.checkout;


import com.entri.checkout.dto.CheckoutDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CheckoutController implements CheckoutApi {

    private final CheckoutService checkoutService;

    @Override
    public CheckoutResponse checkout(final String reservationId, final CheckoutDetails checkoutDetails) {
        return checkoutService.checkout(reservationId, checkoutDetails);
    }

    @Override
    public ResponseEntity<Void> handleWebhook(final Map<String, String> headers, final String payload) {
        checkoutService.handleWebhook(headers, payload);
        return ResponseEntity.ok().build();
    }
}
