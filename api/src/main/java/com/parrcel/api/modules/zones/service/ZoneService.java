package com.parrcel.api.modules.zones.service;

import com.parrcel.api.modules.zones.entity.Zone;
import com.parrcel.api.modules.zones.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }
}
