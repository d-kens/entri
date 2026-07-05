package com.entri.common.dto;

import com.entri.modules.users.entity.Role;

public record AuthenticatedUser(
        String userExternalKey,
        Role role
) {
    public boolean isPlatformAdmin() {
        return role == Role.PLATFORM_ADMIN;
    }
}
