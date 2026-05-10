package com.puuul.api.client.novu;

import com.puuul.api.modules.users.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NovuSubscriberListener {
    private final NovuClient novuClient;

    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        novuClient.createSubscriber(event.user());
    }
}
