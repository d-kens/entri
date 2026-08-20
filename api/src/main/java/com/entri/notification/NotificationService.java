package com.entri.notification;

import com.entri.integrations.novu.NovuClient;
import com.entri.integrations.novu.WorkflowType;
import com.entri.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NovuClient novuClient;

    @EventListener
    public void onNotification(NotificationEvent event) {
        novuClient.triggerWorkflow(toWorkflowType(event.type()), event.subscriberId(), event.payload());
    }

    private WorkflowType toWorkflowType(NotificationType type) {
        return switch (type) {
            case PASSWORD_RESET -> WorkflowType.PASSWORD_RESET;
            case UPDATED_PASSWORD -> WorkflowType.UPDATED_PASSWORD;
        };
    }
}
