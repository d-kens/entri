package com.parrcel.api.modules.deliveries.controller;

import com.parrcel.api.modules.deliveries.service.DeliveryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
}
