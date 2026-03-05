package com.parrcel.api.modules.deliveries.listeners;

import com.parrcel.api.modules.deliveries.service.DeliveryService;
import com.parrcel.api.modules.payment.enums.PaymentType;
import com.parrcel.api.modules.payment.events.PaymentEvent;
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
public class DeliveryPaymentListener {
    private final DeliveryService deliveryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentEvent(PaymentEvent event) {
        if (event.paymentType() != PaymentType.DELIVERY_FEE) {
            return;
        }

        if (event.isSuccess()) {
            log.info("Handling successful payment for delivery: {}", event.referenceId());
            deliveryService.markDeliveryAsPaid(event.referenceId());
        } else if (event.isFailure()) {
            log.info("Handling failed payment for delivery: {}", event.referenceId());
            deliveryService.markDeliveryPaymentFailed(event.referenceId());
        }
    }
}
