package com.parrcel.api.modules.zones.controller;

import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.mapper.ParrcelPointMapper;
import com.parrcel.api.modules.zones.service.ParcelPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parcel-points")
public class ParcelPointController {
    private final ParrcelPointMapper parrcelPointMapper;
    private final ParcelPointService parcelPointService;

    @GetMapping
    public List<ParcelPointResponse> getAllParcelPoints() {
        return parcelPointService.getAllParcelPoints()
                .stream()
                .map(parrcelPointMapper::toResponse)
                .toList();
    }
}


