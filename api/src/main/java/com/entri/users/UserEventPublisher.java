package com.entri.users;

import com.entri.rabbitmq.UserEventConfig;
import com.entri.users.dto.UserCreatedMessage;
import com.entri.users.dto.UserUpdatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UserCreatedMessage message) {
        rabbitTemplate.convertAndSend(UserEventConfig.USER_CREATED_EXCHANGE, "", message);
    }

    public void publishUserUpdated(UserUpdatedMessage message) {
        rabbitTemplate.convertAndSend(UserEventConfig.USER_UPDATED_EXCHANGE, UserEventConfig.USER_UPDATED_ROUTING_KEY, message);
    }
}
