package com.oro.api.modules.user.dto;

import com.oro.api.modules.user.entity.Role;

import java.util.Set;

public record AuthResponse(
        String name,
        Set<Role> roles,
        String externalId,
        String accessToken
) {}
