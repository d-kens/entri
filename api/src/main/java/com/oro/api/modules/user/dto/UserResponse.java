package com.oro.api.modules.user.dto;

import java.util.Set;

public record UserResponse(
        String name,
        Set<String> roles,
        String externalId,
        String phoneNumber
) {}
