package com.entri.notification;

import com.entri.notification.dto.NotificationMessage;
import com.entri.rabbitmq.NotificationQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(NotificationMessage message) {
        applicationEventPublisher.publishEvent(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotification(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                NotificationQueueConfig.NOTIFICATION_EXCHANGE,
                NotificationQueueConfig.NOTIFICATION_ROUTING_KEY,
                message
        );
    }
}
