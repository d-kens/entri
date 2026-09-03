package com.entri.checkout;


import com.entri.checkout.dto.CheckoutDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheckoutController implements CheckoutApi {

    private final CheckoutService checkoutService;

    @Override
    public CheckoutResponse checkout(final String reservationId, final CheckoutDetails checkoutDetails) {
        return checkoutService.checkout(reservationId, checkoutDetails);
    }
}
