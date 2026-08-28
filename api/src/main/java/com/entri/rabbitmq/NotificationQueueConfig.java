package com.entri.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationQueueConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";
    public static final String NOTIFICATION_SEND_QUEUE = "notification.send";
    public static final String NOTIFICATION_DLX = "notification.dlx";
    public static final String NOTIFICATION_FAILED_QUEUE = "notification.failed";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(NOTIFICATION_DLX);
    }

    @Bean
    public Queue notificationFailedQueue() {
        return QueueBuilder.durable(NOTIFICATION_FAILED_QUEUE).build();
    }

    @Bean
    public Binding notificationFailedBinding() {
        return BindingBuilder.bind(notificationFailedQueue()).to(notificationDeadLetterExchange()).with(NOTIFICATION_FAILED_QUEUE);
    }

    @Bean
    public Queue notificationSendQueue() {
        return QueueBuilder.durable(NOTIFICATION_SEND_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding notificationSendBinding() {
        return BindingBuilder.bind(notificationSendQueue()).to(notificationExchange()).with(NOTIFICATION_ROUTING_KEY);
    }
}
