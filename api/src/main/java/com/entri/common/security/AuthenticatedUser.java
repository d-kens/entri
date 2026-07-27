package com.entri.common.security;

import com.entri.modules.users.entity.Role;

public record AuthenticatedUser(
        String userExternalKey,
        Role role
) {
    public boolean isPlatformAdmin() {
        return role == Role.PLATFORM_ADMIN;
    }
}
