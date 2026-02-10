package com.parrcel.api.modules.zones.controller;

import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.dto.ZoneResponse;
import com.parrcel.api.modules.zones.mapper.ParrcelPointMapper;
import com.parrcel.api.modules.zones.mapper.ZoneMapper;
import com.parrcel.api.modules.zones.service.ParcelPointService;
import com.parrcel.api.modules.zones.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/zones")
public class ZoneController {
    private final ZoneService zoneService;
    private final ZoneMapper zoneMapper;
    private final ParcelPointService parcelPointService;
    private final ParrcelPointMapper parrcelPointMapper;

    @GetMapping
    public List<ZoneResponse> getAllZones() {
        return zoneService.getAllZones()
                .stream()
                .map(zoneMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}/parcel-points")
    public List<ParcelPointResponse> getParcelPointsByZone(
            @PathVariable Long id
    ) {
        return parcelPointService.getParcelPointsByZoneId(id)
                .stream()
                .map(parrcelPointMapper::toResponse)
                .toList();
    }
}
