package com.parrcel.api.modules.payment.validation;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConditionalPhoneNumberValidator
        implements ConstraintValidator<ValidInitiatePaymentDto, InitiatePaymentDto> {

    @Override
    public boolean isValid(InitiatePaymentDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        // Phone number required for MPESA
        if ("MPESA".equals(dto.paymentMethod())) {
            if (dto.phoneNumber() == null || dto.phoneNumber().isBlank()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "phoneNumber is required for M-Pesa payments"
                ).addPropertyNode("phoneNumber").addConstraintViolation();
                return false;
            }

            if (!dto.phoneNumber().matches("^(07|01)\\d{8}$")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Invalid phone number format (07XXXXXXXX or 01XXXXXXXX)"
                ).addPropertyNode("phoneNumber").addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}