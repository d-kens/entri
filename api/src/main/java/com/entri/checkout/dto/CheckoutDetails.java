package com.entri.checkout.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckoutDetails(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Phone number is required")
        String phoneNumber
) {
}
