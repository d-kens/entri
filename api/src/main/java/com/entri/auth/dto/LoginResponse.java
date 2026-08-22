package com.entri.auth.dto;

import com.entri.users.dto.UserResponse;

public record LoginResponse(
        UserResponse user,
        AccessToken accessToken
) {}
