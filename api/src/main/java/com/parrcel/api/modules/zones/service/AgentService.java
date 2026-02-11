package com.parrcel.api.modules.zones.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.zones.model.Agent;
import com.parrcel.api.modules.zones.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;

    public Agent getAgentPointById(Long id) {
        return agentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("agent point with id " + id + " not found")
        );
    }

    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    public List<Agent> getAgentsByZoneId(Long zoneId) {
        return agentRepository.findByZoneId(zoneId);
    }
}

