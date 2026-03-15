package com.parrcel.api.modules.auth.dto;

import com.parrcel.api.modules.users.validation.ValidPhoneNumber;

public record ForgotPasswordRequest(
        @ValidPhoneNumber
        String phoneNumber
) {}