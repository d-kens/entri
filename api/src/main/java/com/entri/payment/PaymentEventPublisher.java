package com.entri.payment;

import com.entri.rabbitmq.PaymentQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishWebhookResult(PaymentResultEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void publishPayoutResult(PayoutResultEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWebhookResult(PaymentResultEvent event) {
        rabbitTemplate.convertAndSend(
                PaymentQueueConfig.WEBHOOK_EXCHANGE,
                PaymentQueueConfig.WEBHOOK_ROUTING_KEY,
                event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPayoutResult(PayoutResultEvent event) {
        rabbitTemplate.convertAndSend(
                PaymentQueueConfig.PAYOUT_EXCHANGE,
                PaymentQueueConfig.PAYOUT_ROUTING_KEY,
                event);
    }
}
