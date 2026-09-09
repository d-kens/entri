package com.entri.modules.checkout;

import com.entri.checkout.CheckoutResultConsumer;
import com.entri.events.service.EventTicketReservationService;
import com.entri.payment.PaymentResultEvent;
import com.entri.payment.dto.PaymentResult;
import com.entri.payment.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CheckoutResultConsumerTest {

    @Mock EventTicketReservationService eventTicketReservationService;

    @InjectMocks CheckoutResultConsumer checkoutResultConsumer;

    @Test
    void handle_mapsPaymentResultEventAndDelegatesToReservationService() {
        var message = new PaymentResultEvent("ref-123", PaymentStatus.PAID);

        checkoutResultConsumer.handle(message);

        ArgumentCaptor<PaymentResult> captor = ArgumentCaptor.forClass(PaymentResult.class);
        verify(eventTicketReservationService).applyPaymentResult(captor.capture());
        assertThat(captor.getValue().referenceId()).isEqualTo("ref-123");
        assertThat(captor.getValue().status()).isEqualTo(PaymentStatus.PAID);
    }
}
