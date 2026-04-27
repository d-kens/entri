package com.puuul.api.users.dto;

public record LoginResponseDto(
        UserInfo user,
        long expiresIn,
        String accessToken
) {
    public record UserInfo(
            String role,
            String email,
            String externalKey
    ){}
}
