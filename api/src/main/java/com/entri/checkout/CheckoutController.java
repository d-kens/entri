package com.entri.checkout;


import com.entri.checkout.dto.CheckoutRequest;
import com.entri.checkout.dto.CheckoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheckoutController implements CheckoutApi {

    private final CheckoutService checkoutService;

    @Override
    public CheckoutResponse checkout(final String reservationId, final CheckoutRequest checkoutRequest) {
        return checkoutService.checkout(reservationId, checkoutRequest);
    }
}
