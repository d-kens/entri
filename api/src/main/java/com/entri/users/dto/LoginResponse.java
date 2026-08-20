package com.entri.users.dto;

public record LoginResponse(
        UserResponse user,
        AccessToken accessToken
) {}
