package com.entri.checkout;

import com.entri.checkout.dto.CheckoutDetails;
import com.entri.events.entity.EventTicketReservation;
import com.entri.events.service.EventTicketReservationService;
import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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

        if (reservation.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            eventTicketReservationService.confirmFreeReservation(reservation);
            return new CheckoutResponse(ticketsUrl(reservation));
        }

        return new CheckoutResponse(paymentGateway.checkout(buildCheckoutRequest(reservation)));
    }

    private CheckoutRequest buildCheckoutRequest(final EventTicketReservation reservation) {
        return new CheckoutRequest(
                reservation.getFirstName(),
                reservation.getLastName(),
                reservation.getPhoneNumber(),
                reservation.getEmail(),
                reservation.getExternalId(),
                ticketsUrl(reservation),
                reservation.getTotalAmount(),
                reservation.getEvent().getCurrency()
        );
    }

    private String ticketsUrl(final EventTicketReservation reservation) {
        return appBaseUrl.stripTrailing() + "/tickets/" + reservation.getExternalId();
    }
}
