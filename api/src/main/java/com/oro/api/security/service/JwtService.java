package com.oro.api.security.service;

import com.oro.api.modules.user.entity.Role;
import com.oro.api.modules.user.entity.User;
import com.oro.api.security.config.JwtConfig;
import com.oro.api.security.model.Jwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;

    private Claims getClaims(String token) {
        return  Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Jwt generateToken(User user, long tokenExpiration) {
        var roleNames = user.getRoles().stream().map(Role::getName).toList();
        var claims = Jwts.claims()
                .subject(user.getId().toString())
                .add("phoneNumber", user.getPhoneNumber())
                .add("name", user.getName())
                .add("roles", roleNames)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .build();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        } catch (JwtException e) {
            return null;
        }
    }

    public Jwt generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration());
    }

    public Jwt generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshTokenExpiration());
    }

}
