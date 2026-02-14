package com.parrcel.api.modules.payment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ConditionalPhoneNumberValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInitiatePaymentDto {
    String message() default "Invalid payment data";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}