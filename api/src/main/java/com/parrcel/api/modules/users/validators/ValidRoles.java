package com.parrcel.api.modules.users.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RolesValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ValidRoles {
    String message() default "role must be one of: {allowedValues}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}