package com.parrcel.api.modules.payment.validation;

import com.parrcel.api.modules.payment.dto.InitiatePaymentDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConditionalPhoneNumberValidator
        implements ConstraintValidator<ValidInitiatePaymentDto, InitiatePaymentDto> {

    @Override
    public boolean isValid(InitiatePaymentDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        if ("MPESA".equals(dto.paymentMethod())) {
            if (dto.phoneNumber() == null || dto.phoneNumber().isBlank()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "phoneNumber is required for M-Pesa payments"
                ).addPropertyNode("phoneNumber").addConstraintViolation();
                return false;
            }

            if (!dto.phoneNumber().matches("^(07|01|2547|2541)\\d{8}$")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Invalid phone number format. Use 07XXXXXXXX, 01XXXXXXXX or 2547XXXXXXXX"
                ).addPropertyNode("phoneNumber").addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}