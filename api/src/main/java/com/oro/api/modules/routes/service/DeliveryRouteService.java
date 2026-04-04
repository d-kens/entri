package com.oro.api.modules.routes.service;

import com.oro.api.common.exception.NotFoundException;
import com.oro.api.modules.routes.dto.CreateRouteDto;
import com.oro.api.modules.routes.dto.RouteResponseDto;
import com.oro.api.modules.routes.entity.DeliveryRoute;
import com.oro.api.modules.routes.repository.DeliveryRouteRepository;
import com.oro.api.modules.zones.entity.Agent;
import com.oro.api.modules.zones.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryRouteService {

    private final DeliveryRouteRepository routeRepository;
    private final AgentRepository agentRepository;

    @Transactional
    public DeliveryRoute createRoute(CreateRouteDto dto) {
        DeliveryRoute route = new DeliveryRoute();
        route.setName(dto.name());
        route.setDescription(dto.description());
        return routeRepository.save(route);
    }

    @Transactional(readOnly = true)
    public List<DeliveryRoute> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DeliveryRoute getRouteByExternalId(String externalId) {
        return routeRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Route not found: " + externalId));
    }

    @Transactional
    public Agent assignAgentToRoute(String routeExternalId, Long agentId) {
        DeliveryRoute route = getRouteByExternalId(routeExternalId);
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));

        agent.setRoute(route);
        return agentRepository.save(agent);
    }

    @Transactional
    public Agent removeAgentFromRoute(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));

        agent.setRoute(null);
        return agentRepository.save(agent);
    }

    @Transactional(readOnly = true)
    public List<Agent> getAgentsForRoute(String routeExternalId) {
        DeliveryRoute route = getRouteByExternalId(routeExternalId);
        return agentRepository.findAllByRoute(route);
    }

    public RouteResponseDto toDto(DeliveryRoute route) {
        return new RouteResponseDto(
                route.getExternalId(),
                route.getName(),
                route.getDescription(),
                route.getIsActive()
        );
    }
}
