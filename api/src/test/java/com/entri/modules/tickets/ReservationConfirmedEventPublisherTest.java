package com.entri.modules.tickets;

import com.entri.rabbitmq.ReservationConfirmedQueueConfig;
import com.entri.tickets.ReservationConfirmedEventPublisher;
import com.entri.tickets.dto.ReservationConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReservationConfirmedEventPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;
    @Mock ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks ReservationConfirmedEventPublisher reservationConfirmedEventPublisher;

    @Test
    void publishReservationConfirmed_publishesSpringApplicationEventWithoutTouchingRabbit() {
        var message = new ReservationConfirmedEvent("res-ext-123", "org-ext-456", BigDecimal.TEN, "KES");

        reservationConfirmedEventPublisher.publishReservationConfirmed(message);

        verify(applicationEventPublisher).publishEvent(message);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void onReservationConfirmed_sendsMessageToReservationConfirmedExchange() {
        var message = new ReservationConfirmedEvent("res-ext-123", "org-ext-456", BigDecimal.TEN, "KES");

        reservationConfirmedEventPublisher.onReservationConfirmed(message);

        verify(rabbitTemplate).convertAndSend(
                ReservationConfirmedQueueConfig.RESERVATION_CONFIRMED_EXCHANGE,
                ReservationConfirmedQueueConfig.RESERVATION_CONFIRMED_ROUTING_KEY,
                message
        );
    }
}
