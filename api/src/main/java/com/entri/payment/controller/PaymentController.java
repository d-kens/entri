package com.entri.payment.controller;

import com.entri.payment.controller.api.PaymentApi;
import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.dto.CheckoutResponse;
import com.entri.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    public CheckoutResponse checkout(final CheckoutRequest checkoutRequest) {
        return paymentService.checkout(checkoutRequest);
    }
}
