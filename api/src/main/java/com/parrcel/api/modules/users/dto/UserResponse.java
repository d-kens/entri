package com.parrcel.api.modules.users.dto;


public record UserResponse(
        Long id,
        String userName,
        String phoneNumber,
        String email,
        String role
) {}