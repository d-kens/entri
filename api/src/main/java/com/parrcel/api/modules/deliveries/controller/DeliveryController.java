package com.parrcel.api.modules.deliveries.controller;

import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
import com.parrcel.api.modules.deliveries.model.Delivery;
import com.parrcel.api.modules.deliveries.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private ResponseEntity<Delivery> create(
            @AuthenticationPrincipal long userId,
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateDeliveryDto createDeliveryDto
    ) {
        var delivery = deliveryService.create(createDeliveryDto, userId);
        var uri = uriComponentsBuilder.path("/deliveries/{deliveryId}")
                .buildAndExpand(delivery.getId())
                .toUri();

        return ResponseEntity.created(uri).body(delivery);
    }
}
