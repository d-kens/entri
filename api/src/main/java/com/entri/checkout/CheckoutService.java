package com.entri.checkout;


import com.entri.checkout.dto.CheckoutRequest;
import com.entri.checkout.dto.CheckoutResponse;
import com.entri.events.service.EventTicketReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final EventTicketReservationService eventTicketReservationService;
    private final PaymentGateway paymentGateway;

    @Transactional
    CheckoutResponse checkout(final String reservationId, final CheckoutRequest checkoutRequest) {
        var eventTicketReservation = eventTicketReservationService.getPendingReservation(reservationId);

        eventTicketReservation.setFirstName(checkoutRequest.firstName());
        eventTicketReservation.setLastName(checkoutRequest.lastName());
        eventTicketReservation.setEmail(checkoutRequest.email());
        eventTicketReservation.setPhoneNumber(checkoutRequest.phoneNumber());

        return paymentGateway.checkout(eventTicketReservation);
    }
}
