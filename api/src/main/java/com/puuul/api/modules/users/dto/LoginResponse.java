package com.puuul.api.modules.users.dto;

public record LoginResponse(
        UserResponse user,
        AccessToken accessToken
) {}
