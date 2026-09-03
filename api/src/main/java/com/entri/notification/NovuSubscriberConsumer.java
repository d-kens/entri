package com.entri.notification;

import com.entri.notification.novu.NovuClient;
import com.entri.notification.novu.NovuSubscriber;
import com.entri.rabbitmq.UserEventConfig;
import com.entri.users.dto.UserCreatedEvent;
import com.entri.users.dto.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NovuSubscriberConsumer {

    private final NovuClient novuClient;

    @RabbitListener(queues = UserEventConfig.USER_CREATED_SUBSCRIBER_QUEUE)
    public void onUserCreated(UserCreatedEvent message) {
        novuClient.createSubscriber(new NovuSubscriber(
                message.externalKey(),
                message.firstName(),
                message.lastName(),
                message.email(),
                message.phoneNumber()
        ));
    }

    @RabbitListener(queues = UserEventConfig.USER_UPDATED_SUBSCRIBER_QUEUE)
    public void onUserUpdated(UserUpdatedEvent message) {
        novuClient.createSubscriber(new NovuSubscriber(
                message.externalKey(),
                message.firstName(),
                message.lastName(),
                message.email(),
                message.phoneNumber()
        ));
    }
}
