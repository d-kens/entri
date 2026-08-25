package com.entri.notification;

import com.entri.notification.novu.NovuClient;
import com.entri.notification.novu.NovuSubscriber;
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
        var user = event.user();
        novuClient.createSubscriber(new NovuSubscriber(
                user.getExternalKey().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        ));
    }
}
