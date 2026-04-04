package com.oro.api.modules.routes.controller;

import com.oro.api.modules.routes.dto.CreateRouteDto;
import com.oro.api.modules.routes.dto.RouteResponseDto;
import com.oro.api.modules.routes.service.DeliveryRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class DeliveryRouteController {

    private final DeliveryRouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponseDto> createRoute(@Valid @RequestBody CreateRouteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeService.toDto(routeService.createRoute(dto)));
    }

    @GetMapping
    public ResponseEntity<List<RouteResponseDto>> getAllRoutes() {
        return ResponseEntity.ok(
                routeService.getAllRoutes().stream().map(routeService::toDto).toList()
        );
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<RouteResponseDto> getRoute(@PathVariable String externalId) {
        return ResponseEntity.ok(routeService.toDto(routeService.getRouteByExternalId(externalId)));
    }

    @PostMapping("/{routeExternalId}/agents/{agentId}")
    public ResponseEntity<Void> assignAgent(
            @PathVariable String routeExternalId,
            @PathVariable Long agentId) {
        routeService.assignAgentToRoute(routeExternalId, agentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/agents/{agentId}/route")
    public ResponseEntity<Void> removeAgentFromRoute(@PathVariable Long agentId) {
        routeService.removeAgentFromRoute(agentId);
        return ResponseEntity.ok().build();
    }
}
