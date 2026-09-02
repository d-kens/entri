package com.entri.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationConfirmedQueueConfig {

    public static final String RESERVATION_CONFIRMED_EXCHANGE = "reservation.confirmed";
    public static final String RESERVATION_CONFIRMED_ROUTING_KEY = "reservation.confirmed";
    public static final String TICKET_GENERATION_QUEUE = "reservation.confirmed.ticket-generation";
    public static final String RESERVATION_EVENTS_DLX = "reservation.events.dlx";
    public static final String TICKET_GENERATION_FAILED_QUEUE = "reservation.confirmed.ticket-generation.failed";

    @Bean
    public DirectExchange reservationEventsDeadLetterExchange() {
        return new DirectExchange(RESERVATION_EVENTS_DLX);
    }

    @Bean
    public Queue ticketGenerationFailedQueue() {
        return QueueBuilder.durable(TICKET_GENERATION_FAILED_QUEUE).build();
    }

    @Bean
    public Binding ticketGenerationFailedBinding() {
        return BindingBuilder.bind(ticketGenerationFailedQueue())
                .to(reservationEventsDeadLetterExchange())
                .with(TICKET_GENERATION_FAILED_QUEUE);
    }

    @Bean
    public DirectExchange reservationConfirmedExchange() {
        return new DirectExchange(RESERVATION_CONFIRMED_EXCHANGE);
    }

    @Bean
    public Queue ticketGenerationQueue() {
        return QueueBuilder.durable(TICKET_GENERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RESERVATION_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", TICKET_GENERATION_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding ticketGenerationBinding() {
        return BindingBuilder.bind(ticketGenerationQueue())
                .to(reservationConfirmedExchange())
                .with(RESERVATION_CONFIRMED_ROUTING_KEY);
    }
}
