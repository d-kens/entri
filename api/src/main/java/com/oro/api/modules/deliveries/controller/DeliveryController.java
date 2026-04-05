package com.oro.api.modules.deliveries.controller;

import com.oro.api.common.dto.PageResponse;
import com.oro.api.modules.deliveries.dto.CreateDeliveryDto;
import com.oro.api.modules.deliveries.dto.DeliveryResponseDto;
import com.oro.api.modules.deliveries.dto.TrackDeliveryResponseDto;
import com.oro.api.modules.deliveries.dto.UpdateDeliveryStatusDto;
import com.oro.api.modules.deliveries.mapper.DeliveryMapper;
import com.oro.api.modules.deliveries.entity.Delivery;
import com.oro.api.modules.deliveries.service.DeliveryService;
import com.oro.api.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('delivery:read')")
    public ResponseEntity<PageResponse<DeliveryResponseDto>> getDeliveries(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());

        Page<Delivery> deliveryPage = deliveryService.getDeliveries(
                principal.getUser().getId(),
                status,
                search,
                pageable
        );

        return ResponseEntity.ok(PageResponse.of(deliveryPage.map(deliveryMapper::toResponseDto)));
    }

    @GetMapping("/{externalId}")
    @PreAuthorize("hasAuthority('delivery:read')")
    public DeliveryResponseDto getDeliveryByExternalId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String externalId
    ) {
        var delivery = deliveryService.getDeliveryByExternalId(externalId, principal.getUser());
        return deliveryMapper.toResponseDto(delivery);
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<TrackDeliveryResponseDto> trackDelivery(
            @PathVariable String trackingNumber
    ) {
        TrackDeliveryResponseDto responseDto = deliveryService.trackByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{externalId}/status")
    @PreAuthorize("hasAuthority('delivery:update')")
    public ResponseEntity<DeliveryResponseDto> updateStatus(
            @PathVariable String externalId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateDeliveryStatusDto dto
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        var delivery = deliveryService.updateDeliveryStatus(
                externalId, dto.status(), dto.cancellationReason(), isAdmin);

        return ResponseEntity.ok(deliveryMapper.toResponseDto(delivery));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('delivery:create')")
    public ResponseEntity<DeliveryResponseDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateDeliveryDto createDeliveryDto
    ) {
        var delivery = deliveryService.create(createDeliveryDto, principal.getUser().getId());
        var uri = uriComponentsBuilder.path("/deliveries/{deliveryId}")
                .buildAndExpand(delivery.getId())
                .toUri();

        return ResponseEntity.created(uri).body(deliveryMapper.toResponseDto(delivery));
    }
}
