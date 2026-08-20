package com.entri.shared.security;

import com.entri.users.entity.Role;

public record AuthenticatedUser(
        String userExternalKey,
        Role role
) {
    public boolean isPlatformAdmin() {
        return role == Role.PLATFORM_ADMIN;
    }
}
