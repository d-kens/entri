package com.oro.api.modules.zones.controller;

import com.oro.api.modules.zones.dto.AgentResponse;
import com.oro.api.modules.zones.mapper.AgentMapper;
import com.oro.api.modules.zones.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agents")
public class AgentController {
    private final AgentMapper agentMapper;
    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> getAllParcelPoints() {
        return agentService.getAllAgents()
                .stream()
                .map(agentMapper::toResponse)
                .toList();
    }
}


