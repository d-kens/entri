package com.parrcel.api.modules.notification.service;

import com.parrcel.api.common.exception.NotificationDeliveryException;
import com.parrcel.api.modules.notification.dto.NotificationDto;
import com.parrcel.api.modules.notification.dto.SubscriberDto;
import com.parrcel.api.modules.notification.events.CreateSubscriberEvent;
import com.parrcel.api.modules.notification.events.SendNotificationEvent;
import com.parrcel.api.modules.notification.novu.NovuClient;
import com.parrcel.api.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NovuClient novuClient;

    @Async
    @EventListener
    public void sendNotification(SendNotificationEvent sendNotificationEvent) {
        NotificationDto notification = new NotificationDto();

        notification.setPayload(sendNotificationEvent.payload());
        notification.setSubscriber(toSubscriber(sendNotificationEvent.user()));
        notification.setWorkflowIdentifier(sendNotificationEvent.notificationType().toString());

        try {
            novuClient.triggerNotification(notification);

            log.info(
                    "Notification sent. type={}, userId={}",
                    sendNotificationEvent.notificationType().toString(),
                    sendNotificationEvent.user().getId()
            );
        } catch (NotificationDeliveryException exception) {
            log.error(
                    "Failed to send notification. type={}, userId={}",
                    sendNotificationEvent.notificationType().toString(),
                    sendNotificationEvent.user().getId(),
                    exception
            );
            // Swallow exception - notification failure shouldn't break the main flow
        }
    }

    @Async
    @EventListener
    public void createNotificationSubscriber(CreateSubscriberEvent event) {
        var dto = toSubscriber(event.user());

        try {
            novuClient.createSubscriber(dto);

            log.info(
                    "Subscriber creation successful. userId={}",
                    event.user().getId()
            );
        } catch (NotificationDeliveryException exception) {
            log.error(
                    "Failed to create subscriber. userId={}",
                    event.user().getId(),
                    exception
            );
            throw exception;
        }
    }


    private SubscriberDto toSubscriber(User user) {
        SubscriberDto dto = new SubscriberDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getUserName());
        dto.setLastName("");
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }
}