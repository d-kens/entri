package com.entri.checkout;

import com.entri.events.service.EventTicketReservationService;
import com.entri.payment.dto.PaymentResult;
import com.entri.rabbitmq.PaymentQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckoutResultConsumer {

    private final EventTicketReservationService eventTicketReservationService;

    @RabbitListener(queues = PaymentQueueConfig.WEBHOOK_PROCESS_QUEUE)
    public void handle(PaymentResultMessage message) {
        eventTicketReservationService.applyPaymentResult(
                new PaymentResult(message.referenceId(), message.status()));
    }
}
