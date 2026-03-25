package com.parrcel.api.modules.payment.controller;

import com.parrcel.api.modules.payment.providers.MpesaProvider;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaStkCallback;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("mpesa")
@RequiredArgsConstructor
public class MpesaController {

    private final MpesaProvider mpesaProvider;

    @PostMapping("/stk/callback")
    public ResponseEntity<Void> mpesaStkCallback(@RequestBody MpesaStkCallback mpesaStkCallback) {
        mpesaProvider.handleStkCallback(mpesaStkCallback);
        return ResponseEntity.ok().build();
    }
}
