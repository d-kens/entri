package com.parrcel.api.modules.zones.service;

import com.parrcel.api.common.dto.PaginationResponse;
import com.parrcel.api.modules.zones.dto.ZoneResponse;
import com.parrcel.api.modules.zones.model.Zone;
import com.parrcel.api.modules.zones.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;

    public List<ZoneResponse> getAllZones() {

        var zones = zoneRepository.findAll();

        return zones
                .stream()
                .map(zone -> new ZoneResponse(
                        zone.getId(),
                        zone.getZoneName(),
                        zone.getCity(),
                        zone.getIsActive(),
                        zone.getIsCbd(),
                        zone.getCreatedAt(),
                        zone.getUpdatedAt()
                ))
                .toList();

    }
}

