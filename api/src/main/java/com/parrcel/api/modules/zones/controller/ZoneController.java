package com.parrcel.api.modules.zones.controller;

import com.parrcel.api.common.dto.PaginationResponse;
import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.dto.ZoneResponse;
import com.parrcel.api.modules.zones.service.ParcelPointService;
import com.parrcel.api.modules.zones.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/zones")
public class ZoneController {
    private final ZoneService zoneService;
    private final ParcelPointService parcelPointService;

    @GetMapping
    public ResponseEntity<PaginationResponse<ZoneResponse>> getAllZones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(zoneService.getAllZones(pageable));
    }

    @GetMapping("/{id}/parcel-points")
    public ResponseEntity<PaginationResponse<ParcelPointResponse>> getParcelPointsByZone(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(parcelPointService.getParcelPointsByZoneId(id, pageable));
    }
}



