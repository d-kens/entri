package com.parrcel.api.modules.payment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentMethodValidator.class)
@Target(ElementType.FIELD)
public @interface ValidPaymentMethod {
    String message() default "paymentMethod must be one of: {allowedValues}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
