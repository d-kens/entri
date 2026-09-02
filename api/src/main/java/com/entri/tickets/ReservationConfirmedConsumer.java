package com.entri.tickets;

import com.entri.rabbitmq.ReservationEventConfig;
import com.entri.tickets.dto.ReservationConfirmedMessage;
import com.entri.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationConfirmedConsumer {

    private final TicketService ticketService;

    @RabbitListener(queues = ReservationEventConfig.TICKET_GENERATION_QUEUE)
    public void handle(ReservationConfirmedMessage message) {
        ticketService.generateTickets(message.reservationExternalId());
    }
}
