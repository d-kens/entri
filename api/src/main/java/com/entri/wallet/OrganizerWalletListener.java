package com.entri.wallet;

import com.entri.users.entity.Role;
import com.entri.users.event.UserCreatedEvent;
import com.entri.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizerWalletListener {

    private final WalletService walletService;
    private final UserRepository userRepository;

    @EventListener
    public void onUserCreated(final UserCreatedEvent event) {
        var user = event.user();
        if (user.getRole() != Role.PLATFORM_USER) return;

        var walletId = walletService.createWallet(
                user.getFirstName() + " " + user.getLastName(),
                "KES"
        );
        user.setWalletId(walletId);
        userRepository.save(user);
    }
}
