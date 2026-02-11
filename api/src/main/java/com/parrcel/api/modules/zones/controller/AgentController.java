package com.parrcel.api.modules.zones.controller;

import com.parrcel.api.modules.zones.dto.AgentResponse;
import com.parrcel.api.modules.zones.mapper.ParrcelPointMapper;
import com.parrcel.api.modules.zones.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agents")
public class AgentController {
    private final ParrcelPointMapper parrcelPointMapper;
    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> getAllParcelPoints() {
        return agentService.getAllAgents()
                .stream()
                .map(parrcelPointMapper::toResponse)
                .toList();
    }
}


