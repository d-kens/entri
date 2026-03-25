package com.parrcel.api.modules.users.validators;


import com.parrcel.api.modules.users.entity.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        boolean valid = Arrays.stream(Role.values())
                .anyMatch(role -> role.name().equals(value));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "role must be one of: " + Arrays.toString(Role.values())
            ).addConstraintViolation();
        }

        return valid;

    }
}
