package com.parrcel.api.modules.wallet.listeners;


import com.parrcel.api.modules.deliveries.entity.Delivery;
import com.parrcel.api.modules.deliveries.service.DeliveryService;
import com.parrcel.api.modules.payment.entity.PaymentType;
import com.parrcel.api.modules.payment.events.PaymentEvent;
import com.parrcel.api.modules.wallet.entity.Wallet;
import com.parrcel.api.modules.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletPaymentListener {
    private final WalletService walletService;
    private final DeliveryService deliveryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSuccess(PaymentEvent event) {

        if (event.paymentType() != PaymentType.CASH_ON_DELIVERY_COLLECTION) {
            return;
        }

        if (!event.isSuccess()) {
            log.warn("COD payment failed for payment: {} - Reason: {}",
                    event.paymentId(), event.failureReason());
            return;
        }


        log.info("Handling successful COD payment for payment: {}", event.paymentId());


        try {

            Delivery delivery = deliveryService.getDeliveryByExternalId(event.referenceId());

            if (delivery.isCashCollected()) {
                log.info("Cash already collected for delivery: {} - Skipping duplicate",
                        delivery.getExternalId());
                return;
            }

            if (!delivery.isCollectCash()) {
                log.error("Payment {} references delivery {} without COD enabled",
                        event.paymentId(), delivery.getExternalId());
                return;
            }

            Wallet merchantWallet = walletService.getOrCreateWallet(delivery.getUser().getId());

            walletService.creditWallet(
                    merchantWallet,
                    event.amount(),
                    event.referenceId(),
                    "COD collection for delivery " + delivery.getTrackingNumber()
            );

            deliveryService.markCashCollected(delivery.getExternalId());

            log.info("Merchant wallet credited for COD payment: {} - Amount: {}",
                    event.paymentId(), event.amount());

        } catch (Exception e) {
            log.error("CRITICAL: Failed to credit merchant wallet for payment: {} - Manual intervention required",
                    event.paymentId(), e);
        }

    }

}
