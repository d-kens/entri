package com.parrcel.api.modules.payment.controller;


import com.parrcel.api.modules.payment.dto.InitiatePaymentResponse;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallbackDto;
import com.parrcel.api.modules.payment.service.PaymentService;
import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

    @PostMapping("/mpesa/callback")
    public ResponseEntity<Void> mpesaStkCallback(
            @RequestBody MpesaStkCallbackDto mpesaStkCallbackDto
    ) {
        log.info("Received M-Pesa STK callback: {}", mpesaStkCallbackDto);
        paymentService.handleMpesaCallback(mpesaStkCallbackDto);
        return ResponseEntity.ok().build();
    }
}