package com.entri.modules.users.dto;

public record LoginResponse(
        UserResponse user,
        AccessToken accessToken
) {}
