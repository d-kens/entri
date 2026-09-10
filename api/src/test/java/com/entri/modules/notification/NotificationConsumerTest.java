package com.entri.modules.notification;

import com.entri.notification.NotificationConsumer;
import com.entri.notification.NotificationType;
import com.entri.notification.dto.NotificationEvent;
import com.entri.notification.dto.NotificationRecipient;
import com.entri.notification.novu.NovuClient;
import com.entri.notification.novu.WorkflowType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock NovuClient novuClient;

    @InjectMocks NotificationConsumer notificationConsumer;

    @Test
    void handle_passwordReset_triggersPasswordResetWorkflow() {
        var payload = Map.<String, Object>of("token", "abc");
        var message = new NotificationEvent(NotificationType.PASSWORD_RESET, "user-123", payload);

        notificationConsumer.handle(message);

        verify(novuClient).triggerWorkflow(WorkflowType.PASSWORD_RESET, "user-123", payload, null);
    }

    @Test
    void handle_updatedPassword_triggersUpdatedPasswordWorkflow() {
        var payload = Map.<String, Object>of();
        var message = new NotificationEvent(NotificationType.UPDATED_PASSWORD, "user-123", payload);

        notificationConsumer.handle(message);

        verify(novuClient).triggerWorkflow(WorkflowType.UPDATED_PASSWORD, "user-123", payload, null);
    }

    @Test
    void handle_ticketConfirmation_triggersTicketConfirmationWorkflowWithRecipient() {
        var payload = Map.<String, Object>of("ticketCode", "T-1");
        var recipient = new NotificationRecipient("user-123", "Jane", "Doe", "jane@example.com", "0712345678");
        var message = new NotificationEvent(NotificationType.TICKET_CONFIRMATION, "user-123", payload, recipient);

        notificationConsumer.handle(message);

        verify(novuClient).triggerWorkflow(WorkflowType.TICKET_CONFIRMATION, "user-123", payload, recipient);
    }
}
