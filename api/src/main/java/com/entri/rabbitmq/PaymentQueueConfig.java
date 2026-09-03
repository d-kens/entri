package com.entri.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentQueueConfig {

    public static final String WEBHOOK_EXCHANGE = "payment.webhook";
    public static final String WEBHOOK_ROUTING_KEY = "payment.result";
    public static final String WEBHOOK_PROCESS_QUEUE = "payment.webhook.process";
    public static final String PAYOUT_EXCHANGE = "payment.payout";
    public static final String PAYOUT_ROUTING_KEY = "payment.payout.result";
    public static final String PAYOUT_PROCESS_QUEUE = "payment.payout.process";
    public static final String WEBHOOK_DLX = "payment.dlx";
    public static final String WEBHOOK_FAILED_QUEUE = "payment.webhook.failed";
    public static final String PAYOUT_FAILED_QUEUE = "payment.payout.failed";

    @Bean
    public DirectExchange paymentDeadLetterExchange() {
        return new DirectExchange(WEBHOOK_DLX);
    }

    @Bean
    public Queue paymentWebhookFailedQueue() {
        return QueueBuilder.durable(WEBHOOK_FAILED_QUEUE).build();
    }

    @Bean
    public Binding paymentWebhookFailedBinding() {
        return BindingBuilder.bind(paymentWebhookFailedQueue()).to(paymentDeadLetterExchange()).with(WEBHOOK_FAILED_QUEUE);
    }

    @Bean
    public DirectExchange paymentWebhookExchange() {
        return new DirectExchange(WEBHOOK_EXCHANGE);
    }

    @Bean
    public Queue paymentWebhookProcessQueue() {
        return QueueBuilder.durable(WEBHOOK_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", WEBHOOK_DLX)
                .withArgument("x-dead-letter-routing-key", WEBHOOK_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding paymentWebhookProcessBinding() {
        return BindingBuilder.bind(paymentWebhookProcessQueue()).to(paymentWebhookExchange()).with(WEBHOOK_ROUTING_KEY);
    }

    @Bean
    public Queue payoutFailedQueue() {
        return QueueBuilder.durable(PAYOUT_FAILED_QUEUE).build();
    }

    @Bean
    public Binding payoutFailedBinding() {
        return BindingBuilder.bind(payoutFailedQueue()).to(paymentDeadLetterExchange()).with(PAYOUT_FAILED_QUEUE);
    }

    @Bean
    public DirectExchange payoutExchange() {
        return new DirectExchange(PAYOUT_EXCHANGE);
    }

    @Bean
    public Queue payoutProcessQueue() {
        return QueueBuilder.durable(PAYOUT_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", WEBHOOK_DLX)
                .withArgument("x-dead-letter-routing-key", PAYOUT_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding payoutProcessBinding() {
        return BindingBuilder.bind(payoutProcessQueue()).to(payoutExchange()).with(PAYOUT_ROUTING_KEY);
    }
}
