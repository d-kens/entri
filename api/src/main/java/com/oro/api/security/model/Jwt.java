package com.oro.api.security.model;

import com.oro.api.modules.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Jwt {
    private final Claims claims;
    private final SecretKey secretKey;

    public Jwt(Claims claims, SecretKey secretKey) {
        this.claims = claims;
        this.secretKey = secretKey;
    }

    public boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

    public Long getUserId() {
        return Long.valueOf(claims.getSubject());
    }

    public String getPhoneNumber() {
        return claims.get("phoneNumber", String.class);
    }

    public Set<Role> getRoles() {
        List<String> roleNames = claims.get("roles", List.class);
        return roleNames.stream().map(name -> {
            Role role = new Role();
            role.setName(name);
            return role;
        }).collect(Collectors.toSet());
    }

    public String toString() {
        return Jwts.builder().claims(claims).signWith(secretKey).compact();
    }
}
