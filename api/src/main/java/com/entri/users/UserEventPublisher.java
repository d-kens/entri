package com.entri.users;

import com.entri.rabbitmq.UserEventConfig;
import com.entri.users.dto.UserCreatedMessage;
import com.entri.users.dto.UserUpdatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishUserCreated(UserCreatedMessage message) {
        applicationEventPublisher.publishEvent(message);
    }

    public void publishUserUpdated(UserUpdatedMessage message) {
        applicationEventPublisher.publishEvent(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserCreated(UserCreatedMessage message) {
        rabbitTemplate.convertAndSend(UserEventConfig.USER_CREATED_EXCHANGE, "", message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserUpdated(UserUpdatedMessage message) {
        rabbitTemplate.convertAndSend(UserEventConfig.USER_UPDATED_EXCHANGE, UserEventConfig.USER_UPDATED_ROUTING_KEY, message);
    }
}
