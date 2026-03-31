package com.oro.api.modules.zones.controller;

import com.oro.api.modules.zones.dto.AgentResponse;
import com.oro.api.modules.zones.dto.ZoneResponse;
import com.oro.api.modules.zones.mapper.AgentMapper;
import com.oro.api.modules.zones.mapper.ZoneMapper;
import com.oro.api.modules.zones.service.AgentService;
import com.oro.api.modules.zones.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/zones")
public class ZoneController {
    private final ZoneService zoneService;
    private final ZoneMapper zoneMapper;
    private final AgentService agentService;
    private final AgentMapper agentMapper;

    @GetMapping
    public List<ZoneResponse> getAllZones() {
        return zoneService.getAllZones()
                .stream()
                .map(zoneMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}/agents")
    public List<AgentResponse> getAgentsByZone(
            @PathVariable Long id
    ) {
        return agentService.getAgentsByZoneId(id)
                .stream()
                .map(agentMapper::toResponse)
                .toList();
    }
}
