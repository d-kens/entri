package com.parrcel.api.modules.payment.controller;


import com.parrcel.api.modules.payment.dto.PaymentInitiationDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("payments")
public class PaymentController {

    @PostMapping("/initiate")
    public String initiatePayment(
            @Valid @RequestBody PaymentInitiationDto dto
            ) {
        return "Payment initiated";
    }


}
