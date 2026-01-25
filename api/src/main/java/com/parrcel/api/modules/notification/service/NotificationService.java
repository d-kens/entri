package com.parrcel.api.modules.notification.service;

import com.parrcel.api.common.exception.NotificationDeliveryException;
import com.parrcel.api.modules.notification.dto.NotificationDto;
import com.parrcel.api.modules.notification.dto.SubscriberDto;
import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.notification.events.NotificationEvent;
import com.parrcel.api.modules.notification.novu.NovuClient;
import com.parrcel.api.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NovuClient novuClient;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            sendNotification(event.getUser(), event.getNotificationType(), event.getPayload());
        } catch (Exception e) {
            log.error(
                    "Failed to handle notification event. type={}, userId={}",
                    event.getNotificationType(),
                    event.getUser().getId(),
                    e
            );
            // Swallow exception - notification failure shouldn't break the main flow
        }
    }

    public void sendNotification(
            User user,
            NotificationType notificationType,
            Map<String, Object> payload
    ) {
        NotificationDto notification = new NotificationDto();

        notification.setPayload(payload);
        notification.setSubscriber(toSubscriber(user));
        notification.setWorkflowIdentifier(notificationType.getNotificationType());

        try {
            novuClient.triggerNotification(notification);

            log.info(
                    "Notification sent. type={}, userId={}",
                    notificationType,
                    user.getId()
            );
        } catch (NotificationDeliveryException exception) {
            log.error(
                    "Failed to send notification. type={}, userId={}",
                    notificationType,
                    user.getId(),
                    exception
            );
            throw exception;
        }
    }

    public void createNotificationSubscriber(User user) {
        var dto = toSubscriber(user);

        try {
            novuClient.createSubscriber(dto);

            log.info(
                    "Subscriber creation successful. userId={}",
                    user.getId()
            );
        } catch (NotificationDeliveryException exception) {
            log.error(
                    "Failed to create subscriber. userId={}",
                    user.getId(),
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