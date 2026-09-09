package com.entri.modules.notification;

import com.entri.notification.NotificationEventPublisher;
import com.entri.notification.NotificationType;
import com.entri.notification.dto.NotificationEvent;
import com.entri.rabbitmq.NotificationQueueConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;
    @Mock ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks NotificationEventPublisher notificationEventPublisher;

    @Test
    void publish_publishesSpringApplicationEventWithoutTouchingRabbit() {
        var message = new NotificationEvent(NotificationType.PASSWORD_RESET, "user-123", Map.of("token", "abc"));

        notificationEventPublisher.publish(message);

        verify(applicationEventPublisher).publishEvent(message);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void onNotification_sendsMessageToNotificationExchange() {
        var message = new NotificationEvent(NotificationType.TICKET_CONFIRMATION, "user-123", Map.of("ticketCode", "T-1"));

        notificationEventPublisher.onNotification(message);

        verify(rabbitTemplate).convertAndSend(
                NotificationQueueConfig.NOTIFICATION_EXCHANGE,
                NotificationQueueConfig.NOTIFICATION_ROUTING_KEY,
                message
        );
    }
}
