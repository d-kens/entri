package com.entri.users;

import com.entri.rabbitmq.UserEventConfig;
import com.entri.users.dto.UserCreatedEvent;
import com.entri.users.dto.UserUpdatedEvent;
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

    public void publishUserCreated(UserCreatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    public void publishUserUpdated(UserUpdatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserCreated(UserCreatedEvent message) {
        rabbitTemplate.convertAndSend(UserEventConfig.USER_CREATED_EXCHANGE, "", message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserUpdated(UserUpdatedEvent message) {
        rabbitTemplate.convertAndSend(UserEventConfig.USER_UPDATED_EXCHANGE, UserEventConfig.USER_UPDATED_ROUTING_KEY, message);
    }
}
