package com.entri.modules.payment;

import com.entri.payment.PaymentEventPublisher;
import com.entri.payment.PaymentResultEvent;
import com.entri.payment.PayoutResultEvent;
import com.entri.payment.enums.PaymentStatus;
import com.entri.payment.enums.PayoutStatus;
import com.entri.rabbitmq.PaymentQueueConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentEventPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;
    @Mock ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks PaymentEventPublisher paymentEventPublisher;

    @Test
    void publishWebhookResult_publishesSpringApplicationEventWithoutTouchingRabbit() {
        var event = new PaymentResultEvent("ref-123", PaymentStatus.PAID);

        paymentEventPublisher.publishWebhookResult(event);

        verify(applicationEventPublisher).publishEvent(event);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void publishPayoutResult_publishesSpringApplicationEventWithoutTouchingRabbit() {
        var event = new PayoutResultEvent("track-123", PayoutStatus.COMPLETED);

        paymentEventPublisher.publishPayoutResult(event);

        verify(applicationEventPublisher).publishEvent(event);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void onWebhookResult_sendsMessageToWebhookExchange() {
        var event = new PaymentResultEvent("ref-123", PaymentStatus.PAID);

        paymentEventPublisher.onWebhookResult(event);

        verify(rabbitTemplate).convertAndSend(
                PaymentQueueConfig.WEBHOOK_EXCHANGE,
                PaymentQueueConfig.WEBHOOK_ROUTING_KEY,
                event
        );
    }

    @Test
    void onPayoutResult_sendsMessageToPayoutExchange() {
        var event = new PayoutResultEvent("track-123", PayoutStatus.COMPLETED);

        paymentEventPublisher.onPayoutResult(event);

        verify(rabbitTemplate).convertAndSend(
                PaymentQueueConfig.PAYOUT_EXCHANGE,
                PaymentQueueConfig.PAYOUT_ROUTING_KEY,
                event
        );
    }
}
