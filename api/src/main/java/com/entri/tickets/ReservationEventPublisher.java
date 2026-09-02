package com.entri.tickets;

import com.entri.rabbitmq.ReservationEventConfig;
import com.entri.tickets.dto.ReservationConfirmedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishReservationConfirmed(ReservationConfirmedMessage message) {
        applicationEventPublisher.publishEvent(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onReservationConfirmed(ReservationConfirmedMessage message) {
        rabbitTemplate.convertAndSend(
                ReservationEventConfig.RESERVATION_CONFIRMED_EXCHANGE,
                ReservationEventConfig.RESERVATION_CONFIRMED_ROUTING_KEY,
                message
        );
    }
}
