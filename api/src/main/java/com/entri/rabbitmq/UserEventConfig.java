package com.entri.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserEventConfig {

    public static final String USER_CREATED_EXCHANGE = "user.created";
    public static final String USER_CREATED_SUBSCRIBER_QUEUE = "user.created.subscriber";
    public static final String USER_CREATED_WALLET_QUEUE = "user.created.wallet";

    public static final String USER_UPDATED_EXCHANGE = "user.updated";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_UPDATED_SUBSCRIBER_QUEUE = "user.updated.subscriber";

    public static final String USER_EVENTS_DLX = "user.events.dlx";
    public static final String USER_CREATED_SUBSCRIBER_FAILED_QUEUE = "user.created.subscriber.failed";
    public static final String USER_CREATED_WALLET_FAILED_QUEUE = "user.created.wallet.failed";
    public static final String USER_UPDATED_SUBSCRIBER_FAILED_QUEUE = "user.updated.subscriber.failed";

    @Bean
    public DirectExchange userEventsDeadLetterExchange() {
        return new DirectExchange(USER_EVENTS_DLX);
    }

    @Bean
    public Queue userCreatedSubscriberFailedQueue() {
        return QueueBuilder.durable(USER_CREATED_SUBSCRIBER_FAILED_QUEUE).build();
    }

    @Bean
    public Queue userCreatedWalletFailedQueue() {
        return QueueBuilder.durable(USER_CREATED_WALLET_FAILED_QUEUE).build();
    }

    @Bean
    public Queue userUpdatedSubscriberFailedQueue() {
        return QueueBuilder.durable(USER_UPDATED_SUBSCRIBER_FAILED_QUEUE).build();
    }

    @Bean
    public Binding userCreatedSubscriberFailedBinding() {
        return BindingBuilder.bind(userCreatedSubscriberFailedQueue()).to(userEventsDeadLetterExchange()).with(USER_CREATED_SUBSCRIBER_FAILED_QUEUE);
    }

    @Bean
    public Binding userCreatedWalletFailedBinding() {
        return BindingBuilder.bind(userCreatedWalletFailedQueue()).to(userEventsDeadLetterExchange()).with(USER_CREATED_WALLET_FAILED_QUEUE);
    }

    @Bean
    public Binding userUpdatedSubscriberFailedBinding() {
        return BindingBuilder.bind(userUpdatedSubscriberFailedQueue()).to(userEventsDeadLetterExchange()).with(USER_UPDATED_SUBSCRIBER_FAILED_QUEUE);
    }

    @Bean
    public FanoutExchange userCreatedExchange() {
        return new FanoutExchange(USER_CREATED_EXCHANGE);
    }

    @Bean
    public Queue userCreatedSubscriberQueue() {
        return QueueBuilder.durable(USER_CREATED_SUBSCRIBER_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", USER_CREATED_SUBSCRIBER_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Queue userCreatedWalletQueue() {
        return QueueBuilder.durable(USER_CREATED_WALLET_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", USER_CREATED_WALLET_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding userCreatedSubscriberBinding() {
        return BindingBuilder.bind(userCreatedSubscriberQueue()).to(userCreatedExchange());
    }

    @Bean
    public Binding userCreatedWalletBinding() {
        return BindingBuilder.bind(userCreatedWalletQueue()).to(userCreatedExchange());
    }

    @Bean
    public DirectExchange userUpdatedExchange() {
        return new DirectExchange(USER_UPDATED_EXCHANGE);
    }

    @Bean
    public Queue userUpdatedSubscriberQueue() {
        return QueueBuilder.durable(USER_UPDATED_SUBSCRIBER_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", USER_UPDATED_SUBSCRIBER_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding userUpdatedSubscriberBinding() {
        return BindingBuilder.bind(userUpdatedSubscriberQueue()).to(userUpdatedExchange()).with(USER_UPDATED_ROUTING_KEY);
    }
}
