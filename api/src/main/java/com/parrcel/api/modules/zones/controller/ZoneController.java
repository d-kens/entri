package com.parrcel.api.modules.zones.controller;

import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.dto.ZoneResponse;
import com.parrcel.api.modules.zones.service.ParcelPointService;
import com.parrcel.api.modules.zones.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/zones")
public class ZoneController {
    private final ZoneService zoneService;
    private final ParcelPointService parcelPointService;

    @GetMapping
    public ResponseEntity<List<ZoneResponse>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @GetMapping("/{id}/parcel-points")
    public ResponseEntity<List<ParcelPointResponse>> getParcelPointsByZone(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(parcelPointService.getParcelPointsByZoneId(id));
    }
}
