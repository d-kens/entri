package com.parrcel.api.modules.zones.controller;

import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.service.ParcelPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parcel-points")
public class ParcelPointController {
    private final ParcelPointService parcelPointService;

    @GetMapping
    public ResponseEntity<List<ParcelPointResponse>> getAllParcelPoints() {
        return ResponseEntity.ok(parcelPointService.getAllParcelPoints());
    }
}


