package com.entri.modules.notification;

import com.entri.client.novu.NovuClient;
import com.entri.modules.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NovuClient novuClient;

    @EventListener
    public void onNotification(NotificationEvent event) {
        novuClient.triggerWorkflow(event.workflow(), event.subscriberId(), event.payload());
    }
}
