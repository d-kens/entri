package com.api.modules.notification;

import com.api.client.novu.NovuClient;
import com.api.modules.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NovuClient novuClient;

    @TransactionalEventListener
    public void onNotification(NotificationEvent event) {
        novuClient.triggerWorkflow(event.workflow(), event.subscriberId(), event.payload());
    }
}
