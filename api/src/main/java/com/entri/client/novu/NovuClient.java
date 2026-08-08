package com.entri.client.novu;

import co.novu.Novu;
import co.novu.models.components.CreateSubscriberRequestDto;
import co.novu.models.components.TriggerEventRequestDtoTo2;
import co.novu.models.components.TriggerEventRequestDto;
import co.novu.models.errors.ErrorDto;
import co.novu.models.errors.ValidationErrorDto;
import com.entri.modules.users.entity.User;
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

    void createSubscriber(User user) {
        var subscriberRequest = toCreateSubscriberRequestDto(user);

        try {
            novu.subscribers().create()
                    .body(subscriberRequest)
                    .call();
        } catch (ValidationErrorDto e) {
            log.error("Validation error creating subscriber for subscriberId={}: {}", subscriberRequest.subscriberId(), e.getMessage());
        } catch (ErrorDto e) {
            log.error("API error creating subscriber for subscriberId={}: {}", subscriberRequest.subscriberId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating subscriber for subscriberId={}", subscriberRequest.subscriberId(), e);
        }
    }

    public void triggerWorkflow(WorkflowType workflow, String subscriberId, Map<String, Object> payload) {
        var request = TriggerEventRequestDto.builder()
                .workflowId(workflow.getWorkflowId())
                .to(TriggerEventRequestDtoTo2.of(subscriberId))
                .payload(payload)
                .build();

        try {
            novu.trigger(request);
        } catch (ValidationErrorDto e) {
            log.error("Validation error triggering workflow={} for subscriberId={}: {}", workflow, subscriberId, e.getMessage());
        } catch (ErrorDto e) {
            log.error("API error triggering workflow={} for subscriberId={}: {}", workflow, subscriberId, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error triggering workflow={} for subscriberId={}", workflow, subscriberId, e);
        }
    }

    private CreateSubscriberRequestDto toCreateSubscriberRequestDto(User user) {
        return CreateSubscriberRequestDto.builder()
                .subscriberId(user.getExternalKey().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .locale(subscriberLocale)
                .timezone(subscriberTimezone)
                .build();
    }
}
