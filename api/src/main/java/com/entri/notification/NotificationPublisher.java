package com.entri.notification;

import com.entri.notification.dto.NotificationMessage;
import com.entri.rabbitmq.NotificationQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationMessage message) {
        rabbitTemplate.convertAndSend(NotificationQueueConfig.NOTIFICATION_EXCHANGE, NotificationQueueConfig.NOTIFICATION_ROUTING_KEY, message);
    }
}
