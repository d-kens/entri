package com.entri.checkout;

import com.entri.checkout.dto.PaymentResult;
import com.entri.checkout.dto.PaymentResultMessage;
import com.entri.rabbitmq.PaymentQueueConfig;
import com.entri.events.service.EventTicketReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentWebhookConsumer {

    private final EventTicketReservationService eventTicketReservationService;

    @RabbitListener(queues = PaymentQueueConfig.WEBHOOK_PROCESS_QUEUE)
    public void handle(PaymentResultMessage message) {
        eventTicketReservationService.applyPaymentResult(
                new PaymentResult(message.reservationExternalId(), message.status())
        );
    }
}
