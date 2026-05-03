package com.puuul.api.modules.notification;

import co.novu.Novu;
import co.novu.models.components.CreateSubscriberRequestDto;
import co.novu.models.errors.ErrorDto;
import co.novu.models.errors.ValidationErrorDto;
import com.puuul.api.modules.users.entity.User;
import com.puuul.api.modules.users.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovuService {
    private final Novu novu;

    @Value("${novu.subscriber.locale}")
    private String subscriberLocale;

    @Value("${novu.subscriber.timezone}")
    private String subscriberTimezone;

    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        createSubscriber(event.user());
    }

    private void createSubscriber(User user) {
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
