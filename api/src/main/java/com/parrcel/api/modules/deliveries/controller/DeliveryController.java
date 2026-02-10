package com.parrcel.api.modules.deliveries.controller;


import com.parrcel.api.modules.deliveries.dto.CalcDeliveryFeeDTO;
import com.parrcel.api.modules.deliveries.dto.DeliveryFeeDTO;
import com.parrcel.api.modules.deliveries.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("fee")
    public DeliveryFeeDTO calculateDeliveryFee(
            @Valid @RequestBody CalcDeliveryFeeDTO request
    ) {
        return deliveryService.calculateDeliveryFee(request);
    }
}
