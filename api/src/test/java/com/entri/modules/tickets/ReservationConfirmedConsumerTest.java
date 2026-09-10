package com.entri.modules.tickets;

import com.entri.tickets.ReservationConfirmedConsumer;
import com.entri.tickets.dto.ReservationConfirmedEvent;
import com.entri.tickets.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationConfirmedConsumerTest {

    @Mock TicketService ticketService;

    @InjectMocks ReservationConfirmedConsumer reservationConfirmedConsumer;

    @Test
    void handle_delegatesToTicketServiceWithReservationExternalId() {
        var message = new ReservationConfirmedEvent("res-ext-123", "org-ext-456", BigDecimal.TEN, "KES");

        reservationConfirmedConsumer.handle(message);

        verify(ticketService).generateTickets("res-ext-123");
    }
}
