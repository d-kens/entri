package com.parrcel.api.modules.deliveries.controller;

import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
import com.parrcel.api.modules.deliveries.dto.DeliveryResponseDto;
import com.parrcel.api.modules.deliveries.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@AllArgsConstructor
@RequestMapping("deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    private ResponseEntity<DeliveryResponseDto> create(
            UriComponentsBuilder componentsBuilder,
            @Valid @RequestBody CreateDeliveryDto createDeliveryDto
    ) {
        return ResponseEntity.ok(new DeliveryResponseDto());
    }
}
