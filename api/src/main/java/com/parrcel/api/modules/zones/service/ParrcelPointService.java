package com.parrcel.api.modules.zones.service;

import com.parrcel.api.modules.zones.model.ParrcelPoint;
import com.parrcel.api.modules.zones.repository.ParcelPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParrcelPointService {
    private final ParcelPointRepository parcelPointRepository;

    public List<ParrcelPoint> getAllParcelPoints() {
        return parcelPointRepository.findAll();
    }

    public List<ParrcelPoint> getParcelPointsByZoneId(Long zoneId) {
        return parcelPointRepository.findByZoneId(zoneId);
    }
}

