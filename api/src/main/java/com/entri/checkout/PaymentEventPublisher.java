package com.entri.checkout;

import com.entri.checkout.dto.PaymentResultMessage;
import com.entri.rabbitmq.PaymentQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishWebhookResult(PaymentResultMessage message) {
        rabbitTemplate.convertAndSend(PaymentQueueConfig.WEBHOOK_EXCHANGE, PaymentQueueConfig.WEBHOOK_ROUTING_KEY, message);
    }
}
