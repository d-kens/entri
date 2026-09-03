package com.entri.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public ResponseEntity<Void> handleWebhook(final Map<String, String> headers, final String payload) {
        paymentService.handleWebhook(headers, payload);
        return ResponseEntity.ok().build();
    }
}
