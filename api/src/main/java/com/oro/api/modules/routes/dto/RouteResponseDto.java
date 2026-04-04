package com.oro.api.modules.routes.dto;

public record RouteResponseDto(
        String externalId,
        String name,
        String description,
        Boolean isActive
) {}
