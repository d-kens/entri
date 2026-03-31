package com.oro.api.modules.deliveries.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CashAmountValidator.class)
public @interface ValidCashAmount {
    String message() default "cashAmount must be provided and greater than 0 when collectCash is true";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
