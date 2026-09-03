package com.entri.wallet;

import com.entri.payment.PayoutResultEvent;
import com.entri.rabbitmq.PaymentQueueConfig;
import com.entri.rabbitmq.ReservationConfirmedQueueConfig;
import com.entri.rabbitmq.UserEventConfig;
import com.entri.tickets.dto.ReservationConfirmedEvent;
import com.entri.users.dto.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletService walletService;

    @RabbitListener(queues = UserEventConfig.USER_CREATED_WALLET_QUEUE)
    public void onUserCreated(UserCreatedEvent event) {
        if (!"ORGANIZER".equals(event.role())) {
            return;
        }
        walletService.createWallet(event.externalKey());
    }

    @RabbitListener(queues = ReservationConfirmedQueueConfig.WALLET_CREDIT_QUEUE)
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        walletService.credit(
                event.organizerExternalKey(),
                event.organizerAmount(),
                event.currency(),
                event.reservationExternalId()
        );
    }

    @RabbitListener(queues = PaymentQueueConfig.PAYOUT_PROCESS_QUEUE)
    public void onPayoutResult(PayoutResultEvent event) {
        walletService.applyPayoutResult(event.trackingId(), event.status());
    }
}
