package com.entri.checkout;

import com.entri.checkout.dto.CheckoutDetails;
import com.entri.events.entity.Event;
import com.entri.events.entity.EventTicketReservation;
import com.entri.events.entity.EventTicketReservationStatus;
import com.entri.events.service.EventTicketReservationService;
import com.entri.payment.PaymentGateway;
import com.entri.payment.dto.CheckoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock EventTicketReservationService eventTicketReservationService;
    @Mock PaymentGateway paymentGateway;

    @InjectMocks CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(checkoutService, "appBaseUrl", "https://entri.io");
    }

    private EventTicketReservation buildReservation(BigDecimal totalAmount) {
        var event = Event.builder().externalId("event-ext").currency("KES").build();
        return EventTicketReservation.builder()
                .externalId("reservation-ext")
                .event(event)
                .status(EventTicketReservationStatus.PENDING)
                .totalAmount(totalAmount)
                .build();
    }

    @Test
    void checkout_freeReservation_confirmsReservationAndReturnsTicketsUrl() {
        var reservation = buildReservation(BigDecimal.ZERO);
        when(eventTicketReservationService.getPendingReservation("reservation-ext")).thenReturn(reservation);

        var details = new CheckoutDetails("john@example.com", "Doe", "John", "0712345678");
        var response = checkoutService.checkout("reservation-ext", details);

        assertThat(response.checkoutUrl()).isEqualTo("https://entri.io/tickets/reservation-ext");
        verify(eventTicketReservationService).confirmFreeReservation(reservation);
        verify(paymentGateway, never()).checkout(any());
    }

    @Test
    void checkout_freeReservation_updatesReservationCustomerDetails() {
        var reservation = buildReservation(BigDecimal.ZERO);
        when(eventTicketReservationService.getPendingReservation("reservation-ext")).thenReturn(reservation);

        var details = new CheckoutDetails("john@example.com", "Doe", "John", "0712345678");
        checkoutService.checkout("reservation-ext", details);

        assertThat(reservation.getFirstName()).isEqualTo("John");
        assertThat(reservation.getLastName()).isEqualTo("Doe");
        assertThat(reservation.getEmail()).isEqualTo("john@example.com");
        assertThat(reservation.getPhoneNumber()).isEqualTo("0712345678");
    }

    @Test
    void checkout_paidReservation_delegatesToPaymentGatewayAndReturnsCheckoutUrl() {
        var reservation = buildReservation(BigDecimal.valueOf(500));
        when(eventTicketReservationService.getPendingReservation("reservation-ext")).thenReturn(reservation);
        when(paymentGateway.checkout(any(CheckoutRequest.class))).thenReturn("https://gateway.example.com/pay/xyz");

        var details = new CheckoutDetails("john@example.com", "Doe", "John", "0712345678");
        var response = checkoutService.checkout("reservation-ext", details);

        assertThat(response.checkoutUrl()).isEqualTo("https://gateway.example.com/pay/xyz");
        verify(eventTicketReservationService, never()).confirmFreeReservation(any());
    }

    @Test
    void checkout_paidReservation_buildsCheckoutRequestWithReservationDetails() {
        var reservation = buildReservation(BigDecimal.valueOf(500));
        when(eventTicketReservationService.getPendingReservation("reservation-ext")).thenReturn(reservation);
        when(paymentGateway.checkout(any(CheckoutRequest.class))).thenReturn("https://gateway.example.com/pay/xyz");

        var details = new CheckoutDetails("john@example.com", "Doe", "John", "0712345678");
        checkoutService.checkout("reservation-ext", details);

        var captor = ArgumentCaptor.forClass(CheckoutRequest.class);
        verify(paymentGateway).checkout(captor.capture());
        var request = captor.getValue();
        assertThat(request.firstName()).isEqualTo("John");
        assertThat(request.lastName()).isEqualTo("Doe");
        assertThat(request.email()).isEqualTo("john@example.com");
        assertThat(request.phoneNumber()).isEqualTo("0712345678");
        assertThat(request.reference()).isEqualTo("reservation-ext");
        assertThat(request.redirectUrl()).isEqualTo("https://entri.io/tickets/reservation-ext");
        assertThat(request.amount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(request.currency()).isEqualTo("KES");
    }
}
