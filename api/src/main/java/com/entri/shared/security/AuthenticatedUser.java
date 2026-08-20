package com.entri.shared.security;

import com.entri.shared.security.Role;

public record AuthenticatedUser(
        String userExternalKey,
        Role role
) {
    public boolean isPlatformAdmin() {
        return role == Role.PLATFORM_ADMIN;
    }
}
