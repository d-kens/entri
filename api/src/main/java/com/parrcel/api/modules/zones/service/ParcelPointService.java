package com.parrcel.api.modules.zones.service;

import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.repository.ParcelPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelPointService {
    private final ParcelPointRepository parcelPointRepository;

    public List<ParcelPointResponse> getAllParcelPoints() {
        return parcelPointRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ParcelPointResponse> getParcelPointsByZoneId(Long zoneId) {
        return parcelPointRepository.findByZoneId(zoneId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ParcelPointResponse mapToResponse(com.parrcel.api.modules.zones.model.ParcelPoint parcelPoint) {
        return new ParcelPointResponse(
                parcelPoint.getId(),
                parcelPoint.getName(),
                parcelPoint.getZone().getId(),
                parcelPoint.getZone().getZoneName(),
                parcelPoint.getUser().getId(),
                parcelPoint.getUser().getUserName(),
                parcelPoint.getLatitude(),
                parcelPoint.getLongitude(),
                parcelPoint.getAddressDescription(),
                parcelPoint.getIsActive(),
                parcelPoint.getPhone(),
                parcelPoint.getOpeningTime(),
                parcelPoint.getClosingTime(),
                parcelPoint.getCreatedAt(),
                parcelPoint.getUpdatedAt()
        );
    }
}

