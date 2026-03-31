package com.oro.api.common.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {

        if (value == null || value.isBlank()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "phoneNumber is required"
            ).addPropertyNode("phoneNumber").addConstraintViolation();
            return false;
        }

        if (!value.matches("^(07|01|2547|2541)\\d{8}$")) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Invalid phone number format. Use 07XXXXXXXX, 01XXXXXXXX or 2547XXXXXXXX"
            ).addPropertyNode("phoneNumber").addConstraintViolation();
            return false;
        }

        return true;
    }
}
