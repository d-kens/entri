package com.parrcel.api.modules.zones.service;

import com.parrcel.api.common.dto.PaginationResponse;
import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.repository.ParcelPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParcelPointService {
    private final ParcelPointRepository parcelPointRepository;

    public PaginationResponse<ParcelPointResponse> getAllParcelPoints(Pageable pageable) {
        Page<ParcelPointResponse> page = parcelPointRepository.findAll(pageable)
                .map(this::mapToResponse);

        return wrapInPaginationResponse(page);
    }

    public PaginationResponse<ParcelPointResponse> getParcelPointsByZoneId(Long zoneId, Pageable pageable) {
        Page<ParcelPointResponse> page = parcelPointRepository.findByZoneId(zoneId, pageable)
                .map(this::mapToResponse);

        return wrapInPaginationResponse(page);
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

    private PaginationResponse<ParcelPointResponse> wrapInPaginationResponse(Page<ParcelPointResponse> page) {
        return new PaginationResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}

