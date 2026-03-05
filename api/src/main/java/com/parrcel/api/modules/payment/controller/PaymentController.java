package com.parrcel.api.modules.payment.controller;


import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallbackDto;
import com.parrcel.api.modules.payment.service.PaymentService;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
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
            @Valid @RequestBody InitiatePaymentDto dto
    ) {
        return paymentService.initiatePayment(dto);
    }

    @GetMapping(value = "/{paymentId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPaymentEvents(@PathVariable String paymentId) {
        log.info("Client subscribing to payment events for paymentId: {}", paymentId);
        return paymentService.streamPaymentEvents(paymentId);
    }

    @PostMapping("/mpesa/callback")
    public ResponseEntity<Void> mpesaStkCallback(
            @RequestBody MpesaStkCallbackDto mpesaStkCallbackDto
    ) {
        log.info("Received M-Pesa STK callback: {}", mpesaStkCallbackDto);
        paymentService.handleMpesaCallback(mpesaStkCallbackDto);
        return ResponseEntity.ok().build();
    }
}