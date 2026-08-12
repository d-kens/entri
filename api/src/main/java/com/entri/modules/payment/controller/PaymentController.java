package com.entri.modules.payment.controller;

import com.entri.modules.payment.controller.api.PaymentApi;
import com.entri.modules.payment.dto.CheckoutRequest;
import com.entri.modules.payment.dto.CheckoutResponse;
import com.entri.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;


    @Override
    public CheckoutResponse checkout(@Valid @RequestBody final CheckoutRequest checkoutRequest) {
        return paymentService.checkout(checkoutRequest);
    }
}
