package com.entri.notification;

import com.entri.notification.dto.NotificationMessage;
import com.entri.notification.novu.NovuClient;
import com.entri.notification.novu.WorkflowType;
import com.entri.rabbitmq.NotificationQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NovuClient novuClient;

    @RabbitListener(queues = NotificationQueueConfig.NOTIFICATION_SEND_QUEUE)
    public void handle(NotificationMessage message) {
        novuClient.triggerWorkflow(toWorkflowType(message.type()), message.subscriberId(), message.payload());
    }

    private WorkflowType toWorkflowType(NotificationType type) {
        return switch (type) {
            case PASSWORD_RESET -> WorkflowType.PASSWORD_RESET;
            case UPDATED_PASSWORD -> WorkflowType.UPDATED_PASSWORD;
        };
    }
}
