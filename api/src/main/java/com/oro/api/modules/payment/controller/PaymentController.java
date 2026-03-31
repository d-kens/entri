package com.oro.api.modules.payment.controller;

import com.oro.api.modules.payment.dto.InitiatePaymentRequest;
import com.oro.api.modules.payment.dto.InitiatePaymentResponse;
import com.oro.api.modules.payment.providers.dto.mpesa.MpesaStkCallback;
import com.oro.api.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController {


    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public InitiatePaymentResponse initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest dto
    ) {
        return paymentService.initiatePayment(dto);
    }

    @PostMapping("/stk/callback")
    public ResponseEntity<Void> mpesaStkCallback(@RequestBody MpesaStkCallback mpesaStkCallback) {
        paymentService.handleStkCallback(mpesaStkCallback);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/{paymentId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPaymentEvents(@PathVariable String paymentId) {
        log.info("Client subscribing to payment events for paymentId: {}", paymentId);
        return paymentService.streamPaymentEvents(paymentId);
    }
}
