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

    public static final String USER_UPDATED_EXCHANGE = "user.updated";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_UPDATED_SUBSCRIBER_QUEUE = "user.updated.subscriber";

    @Bean
    public FanoutExchange userCreatedExchange() {
        return new FanoutExchange(USER_CREATED_EXCHANGE);
    }

    @Bean
    public Queue userCreatedSubscriberQueue() {
        return QueueBuilder.durable(USER_CREATED_SUBSCRIBER_QUEUE).build();
    }

    @Bean
    public Binding userCreatedSubscriberBinding() {
        return BindingBuilder.bind(userCreatedSubscriberQueue()).to(userCreatedExchange());
    }

    @Bean
    public DirectExchange userUpdatedExchange() {
        return new DirectExchange(USER_UPDATED_EXCHANGE);
    }

    @Bean
    public Queue userUpdatedSubscriberQueue() {
        return QueueBuilder.durable(USER_UPDATED_SUBSCRIBER_QUEUE).build();
    }

    @Bean
    public Binding userUpdatedSubscriberBinding() {
        return BindingBuilder.bind(userUpdatedSubscriberQueue()).to(userUpdatedExchange()).with(USER_UPDATED_ROUTING_KEY);
    }
}
