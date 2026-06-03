package com.entri.common.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.time.Instant;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;

    @Override
    public void initialize(ValidDateRange annotation) {
        this.startField = annotation.startField();
        this.endField = annotation.endField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        var wrapper = new BeanWrapperImpl(value);
        var start = (Instant) wrapper.getPropertyValue(startField);
        var end = (Instant) wrapper.getPropertyValue(endField);

        if (start == null || end == null) {
            return true; // handled by @NotNull on each field
        }

        if (end.isBefore(start)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("End time must be on or after start time")
                    .addPropertyNode(endField)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
