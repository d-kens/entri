package com.oro.api.modules.payment.validation;

import com.oro.api.modules.payment.entity.PaymentMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class PaymentMethodValidator implements ConstraintValidator<ValidPaymentMethod, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean valid = Arrays.stream(PaymentMethod.values())
                .anyMatch(paymentMethod -> paymentMethod.name().equals(value));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "paymentMethod must be one of: " + Arrays.toString(PaymentMethod.values())
            ).addConstraintViolation();
        }

        return valid;
    }
}
