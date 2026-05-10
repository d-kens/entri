package com.api.modules.users.dto;

public record LoginResponse(
        UserResponse user,
        AccessToken accessToken
) {}
