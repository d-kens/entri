package com.entri.checkout;

import com.entri.checkout.dto.CheckoutDetails;
import com.entri.events.service.EventTicketReservationService;
import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    @Value("${app.base-url}")
    private String appBaseUrl;

    private final EventTicketReservationService eventTicketReservationService;
    private final PaymentGateway paymentGateway;

    @Transactional
    CheckoutResponse checkout(final String reservationId, final CheckoutDetails details) {
        var reservation = eventTicketReservationService.getPendingReservation(reservationId);

        reservation.setFirstName(details.firstName());
        reservation.setLastName(details.lastName());
        reservation.setEmail(details.email());
        reservation.setPhoneNumber(details.phoneNumber());

        var paymentRequest = new CheckoutRequest(
                reservation.getFirstName(),
                reservation.getLastName(),
                reservation.getPhoneNumber(),
                reservation.getEmail(),
                reservation.getExternalId(),
                appBaseUrl.stripTrailing() + "/tickets/" + reservation.getExternalId(),
                reservation.getTotalAmount(),
                reservation.getEvent().getCurrency()
        );

        return new CheckoutResponse(paymentGateway.checkout(paymentRequest));
    }
}
