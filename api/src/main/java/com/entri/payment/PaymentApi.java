package com.entri.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/payment")
@Tag(
        name = "Payment",
        description = "Operations for payment processing and notifications"
)
public interface PaymentApi {

    @Operation(
            operationId = "HandleWebhook",
            summary = "Receive a payment gateway webhook",
            description = "Receives and processes payment status notifications from the payment gateway."
    )
    @ApiResponse(responseCode = "200", description = "Webhook received and processed")
    @PostMapping("/webhook")
    ResponseEntity<Void> handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload
    );

}
