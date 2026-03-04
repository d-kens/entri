package com.parrcel.api.modules.deliveries.listeners;


import com.parrcel.api.modules.deliveries.service.DeliveryService;
import com.parrcel.api.modules.payment.enums.PaymentType;
import com.parrcel.api.modules.payment.events.PaymentFailedEvent;
import com.parrcel.api.modules.payment.events.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryPaymentListener {
    private final DeliveryService deliveryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentSuccessEvent event) {
        if (event.paymentType() != PaymentType.DELIVERY_FEE) return;

        log.info("Handling PaymentCompletedEvent for delivery: {}", event.referenceId());
        deliveryService.markDeliveryAsPaid(event.referenceId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent event) {
        if (event.paymentType() != PaymentType.DELIVERY_FEE) return;

        log.info("Handling PaymentFailedEvent for delivery: {}", event.referenceId());
        deliveryService.markDeliveryPaymentFailed(event.referenceId());
    }
}
