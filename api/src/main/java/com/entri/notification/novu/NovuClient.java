package com.entri.notification.novu;

import co.novu.Novu;
import co.novu.models.components.SubscriberPayloadDto;
import co.novu.models.components.TriggerEventRequestDtoTo2;
import co.novu.models.components.TriggerEventRequestDto;
import co.novu.models.errors.ErrorDto;
import co.novu.models.errors.ValidationErrorDto;
import com.entri.notification.dto.NotificationRecipient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NovuClient {
    private final Novu novu;

    @Value("${novu.subscriber.locale}")
    private String subscriberLocale;

    @Value("${novu.subscriber.timezone}")
    private String subscriberTimezone;

    public void triggerWorkflow(WorkflowType workflow, String recipientId, Map<String, Object> payload) {
        triggerWorkflow(workflow, recipientId, payload, null);
    }

    public void triggerWorkflow(WorkflowType workflow, String recipientId, Map<String, Object> payload, NotificationRecipient recipient) {
        TriggerEventRequestDtoTo2 to = recipient != null
                ? TriggerEventRequestDtoTo2.of(toSubscriberPayloadDto(recipient))
                : TriggerEventRequestDtoTo2.of(recipientId);

        var request = TriggerEventRequestDto.builder()
                .workflowId(workflow.getWorkflowId())
                .to(to)
                .payload(payload)
                .build();

        try {
            novu.trigger(request);
        } catch (ValidationErrorDto e) {
            log.error("Validation error triggering workflow={} for recipientId={}: {}", workflow, recipientId, e.getMessage());
        } catch (ErrorDto e) {
            log.error("API error triggering workflow={} for recipientId={}: {}", workflow, recipientId, e.getMessage());
            throw new RuntimeException("Novu API error triggering workflow: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error triggering workflow={} for recipientId={}", workflow, recipientId, e);
            throw e;
        }
    }

    private SubscriberPayloadDto toSubscriberPayloadDto(NotificationRecipient recipient) {
        return SubscriberPayloadDto.builder()
                .subscriberId(recipient.id())
                .firstName(recipient.firstName())
                .lastName(recipient.lastName())
                .email(recipient.email())
                .phone(recipient.phoneNumber())
                .locale(subscriberLocale)
                .timezone(subscriberTimezone)
                .build();
    }
}
