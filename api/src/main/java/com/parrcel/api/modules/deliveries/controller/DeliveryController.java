package com.parrcel.api.modules.deliveries.controller;

import com.parrcel.api.common.dto.PageResponse;
import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
import com.parrcel.api.modules.deliveries.dto.DeliveryResponseDto;
import com.parrcel.api.modules.deliveries.mapper.DeliveryMapper;
import com.parrcel.api.modules.deliveries.model.Delivery;
import com.parrcel.api.modules.deliveries.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@AllArgsConstructor
@RequestMapping("deliveries")
public class DeliveryController {

    private final DeliveryMapper deliveryMapper;
    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<PageResponse<DeliveryResponseDto>> getDeliveries(
            @AuthenticationPrincipal Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Delivery> deliveryPage = deliveryService.getDeliveries(currentUserId, status, search, pageable);

        return ResponseEntity.ok(PageResponse.of(deliveryPage.map(deliveryMapper::toResponseDto)));
    }

    @GetMapping("/{externalId}")
    public DeliveryResponseDto getDeliveryByExternalId(
            @PathVariable String externalId
    ) {
        var delivery = deliveryService.getDeliveryByExternalId(externalId);
        return deliveryMapper.toResponseDto(delivery);
    }

    @PostMapping
    public ResponseEntity<DeliveryResponseDto> create(
            @AuthenticationPrincipal Long userId,
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateDeliveryDto createDeliveryDto
    ) {
        var delivery = deliveryService.create(createDeliveryDto, userId);
        var uri = uriComponentsBuilder.path("/deliveries/{deliveryId}")
                .buildAndExpand(delivery.getId())
                .toUri();

        return ResponseEntity.created(uri).body(deliveryMapper.toResponseDto(delivery));
    }
}
