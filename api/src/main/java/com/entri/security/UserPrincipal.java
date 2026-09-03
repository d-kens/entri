package com.entri.security;

import com.entri.exception.ForbiddenException;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getExternalKey() {
        return user.getExternalKey();
    }

    public boolean isAdmin() {
        return user.getRole() == Role.ADMIN;
    }

    public void assertCanManage(String ownerExternalKey) {
        if (!isAdmin() && !getExternalKey().equals(ownerExternalKey)) {
            throw new ForbiddenException("You are not authorized to perform this action");
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getExternalKey();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
