package com.parrcel.api.modules.auth.dto;

import com.parrcel.api.common.validators.ValidPhoneNumber;

public record ForgotPasswordRequest(
        @ValidPhoneNumber
        String phoneNumber
) {}