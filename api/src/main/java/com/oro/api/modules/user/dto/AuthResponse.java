package com.oro.api.modules.user.dto;

import java.util.Set;

public record AuthResponse(
        String name,
        Set<String> roles,
        Set<String> permissions,
        String externalId,
        String accessToken
) {}
