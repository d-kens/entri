package com.oro.api.modules.user.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoleValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ValidRole {
    String message() default "invalid role";

    String[] excluded() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
