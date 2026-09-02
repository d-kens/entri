package com.entri.checkout;

import com.entri.rabbitmq.PaymentQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckoutEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishWebhookResult(CheckoutResultMessage message) {
        rabbitTemplate.convertAndSend(
                PaymentQueueConfig.WEBHOOK_EXCHANGE,
                PaymentQueueConfig.WEBHOOK_ROUTING_KEY,
                message);
    }
}
