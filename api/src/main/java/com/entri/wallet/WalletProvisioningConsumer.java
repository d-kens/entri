package com.entri.wallet;

import com.entri.rabbitmq.UserEventConfig;
import com.entri.users.dto.UserCreatedMessage;
import com.entri.users.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletProvisioningConsumer {

    private final WalletService walletService;

    @RabbitListener(queues = UserEventConfig.USER_CREATED_WALLET_QUEUE)
    public void onUserCreated(UserCreatedMessage message) {
        if (!Role.ORGANIZER.name().equals(message.role())) {
            return;
        }
        walletService.provisionWalletForOrganizer(message.externalKey());
    }
}
