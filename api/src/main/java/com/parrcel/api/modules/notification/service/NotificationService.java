package com.parrcel.api.modules.notification.service;

import com.parrcel.api.common.exception.NotificationDeliveryException;
import com.parrcel.api.modules.notification.dto.NotificationDto;
import com.parrcel.api.modules.notification.dto.SubscriberDto;
import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.notification.novu.NovuClient;
import com.parrcel.api.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NovuClient novuClient;

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