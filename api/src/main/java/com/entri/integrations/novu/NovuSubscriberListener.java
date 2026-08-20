package com.entri.integrations.novu;

import com.entri.users.event.UserCreatedEvent;
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
