package com.parrcel.api.modules.notification.novu;

import co.novu.api.common.SubscriberRequest;
import co.novu.api.events.requests.TriggerEventRequest;
import co.novu.common.base.Novu;
import co.novu.common.rest.NovuNetworkException;
import com.parrcel.api.common.exception.NotificationDeliveryException;
import com.parrcel.api.modules.notification.config.NovuProperties;
import com.parrcel.api.modules.notification.dto.NotificationDto;
import com.parrcel.api.modules.notification.dto.SubscriberDto;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class NovuClient {
    private final Novu novu;
    private final NovuProperties novuProperties;

    public NovuClient(NovuProperties novuProperties) {
        this.novuProperties = novuProperties;
        this.novu = new Novu(novuProperties.getSecret());
    }

    public void triggerNotification(NotificationDto dto) {
        var request = new TriggerEventRequest();
        request.setName(dto.workflowIdentifier());
        request.setTo(buildSubscriber(dto.subscriber()));
        request.setPayload(dto.payload());
        request.setTransactionId(UUID.randomUUID().toString());

        try {
            novu.triggerEvent(request);
        } catch (NovuNetworkException | IOException exception) {
            throw new NotificationDeliveryException("Failed to send notification", exception);
        }
    }

    public void createSubscriber(SubscriberDto dto) {
        var request = this.buildSubscriber(dto);

        try {
            novu.createSubscriber(request);
        } catch (NovuNetworkException | IOException exception) {
            throw new NotificationDeliveryException("Failed to create subscriber", exception);
        }
    }

    private SubscriberRequest buildSubscriber(SubscriberDto dto) {
        SubscriberRequest subscriber = new SubscriberRequest();
        subscriber.setEmail(dto.email());
        subscriber.setPhone(dto.phoneNumber());
        subscriber.setFirstName(dto.firstName());
        subscriber.setLastName(dto.lastName());
        subscriber.setSubscriberId(dto.id().toString());
        return subscriber;
    }
}