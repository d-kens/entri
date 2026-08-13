package com.entri.modules.payment.service;

import com.entri.common.exception.InvalidReservationStatusException;
import com.entri.common.exception.PaymentProviderException;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.modules.events.entity.EventTicketReservationStatus;
import com.entri.modules.events.repository.EventTicketReservationRepository;
import com.entri.modules.payment.dto.CheckoutRequest;
import com.entri.modules.payment.dto.CheckoutResponse;
import com.entri.modules.payment.entity.Payment;
import com.entri.modules.payment.entity.PaymentStatus;
import com.entri.modules.payment.provider.PaymentProvider;
import com.entri.modules.payment.provider.intasend.IntaSendClient;
import com.entri.modules.payment.provider.intasend.IntaSendCheckoutRequest;
import com.entri.modules.payment.provider.intasend.IntaSendProperties;
import com.entri.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IntaSendClient intaSendClient;
    private final IntaSendProperties intaSendProperties;

    private final PaymentRepository paymentRepository;
    private final EventTicketReservationRepository eventTicketReservationRepository;

    @Transactional
    public CheckoutResponse checkout(final CheckoutRequest checkoutRequest) {
        var eventTicketReservation = eventTicketReservationRepository.findByExternalIdForUpdate(checkoutRequest.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + checkoutRequest.reservationId() + " not found"));

        if (eventTicketReservation.getStatus() != EventTicketReservationStatus.PENDING) {
            throw new InvalidReservationStatusException(
                    "Reservation with ID " + checkoutRequest.reservationId()
                            + " is not available for payment"
            );
        }

        var amount = new BigDecimal(1);
        var currency = eventTicketReservation.getEvent().getCurrency();
        var paymentProvider = PaymentProvider.INTA_SEND.toString();

        var payment = Payment.builder()
                .totalAmount(amount)
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .reservation(eventTicketReservation)
                .provider(paymentProvider)
                .build();

        paymentRepository.save(payment);

        var intaSendRequest = new IntaSendCheckoutRequest(
                checkoutRequest.firstName(),
                checkoutRequest.lastName(),
                checkoutRequest.phoneNumber(),
                checkoutRequest.email(),
                payment.getExternalId(),
                "WEBSITE",
                intaSendProperties.redirectUrl(),
                amount,
                currency
        );

        try {
            var intaSendResponse = intaSendClient.createCheckout(intaSendRequest);
            payment.setProviderReference(intaSendResponse.id());
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);
            return new CheckoutResponse(payment.getExternalId(), intaSendResponse.url());
        } catch (PaymentProviderException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw e;
        }
    }
}
