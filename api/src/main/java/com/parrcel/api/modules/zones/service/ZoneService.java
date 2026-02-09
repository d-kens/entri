package com.parrcel.api.modules.zones.service;

import com.parrcel.api.common.dto.PaginationResponse;
import com.parrcel.api.modules.zones.dto.ZoneResponse;
import com.parrcel.api.modules.zones.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;

    public PaginationResponse<ZoneResponse> getAllZones(Pageable pageable) {
        Page<ZoneResponse> page = zoneRepository.findAll(pageable)
                .map(zone -> new ZoneResponse(
                        zone.getId(),
                        zone.getZoneName(),
                        zone.getCity(),
                        zone.getIsActive(),
                        zone.getIsCbd(),
                        zone.getCreatedAt(),
                        zone.getUpdatedAt()
                ));

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

