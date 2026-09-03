package com.entri.tickets;

import com.entri.rabbitmq.ReservationConfirmedQueueConfig;
import com.entri.tickets.dto.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationConfirmedEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishReservationConfirmed(ReservationConfirmedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onReservationConfirmed(ReservationConfirmedEvent message) {
        rabbitTemplate.convertAndSend(
                ReservationConfirmedQueueConfig.RESERVATION_CONFIRMED_EXCHANGE,
                ReservationConfirmedQueueConfig.RESERVATION_CONFIRMED_ROUTING_KEY,
                message
        );
    }

}
