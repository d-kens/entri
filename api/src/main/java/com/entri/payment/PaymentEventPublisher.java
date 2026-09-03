package com.entri.payment;

import com.entri.rabbitmq.PaymentQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishWebhookResult(PaymentResultEvent event) {
        rabbitTemplate.convertAndSend(
                PaymentQueueConfig.WEBHOOK_EXCHANGE,
                PaymentQueueConfig.WEBHOOK_ROUTING_KEY,
                event);
    }

    public void publishPayoutResult(PayoutResultEvent event) {
        rabbitTemplate.convertAndSend(
                PaymentQueueConfig.PAYOUT_EXCHANGE,
                PaymentQueueConfig.PAYOUT_ROUTING_KEY,
                event);
    }
}
