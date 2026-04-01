package com.oro.api.modules.user.validators;

import com.oro.api.modules.user.repository.RoleRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {

    @Autowired
    private RoleRepository roleRepository;

    private List<String> excluded;

    @Override
    public void initialize(ValidRole annotation) {
        this.excluded = Arrays.asList(annotation.excluded());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("role is required").addConstraintViolation();
            return false;
        }

        if (excluded.contains(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("role '" + value + "' is not allowed here").addConstraintViolation();
            return false;
        }

        if (!roleRepository.existsByName(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "invalid role '" + value + "'. Available roles: " + roleRepository.findAll()
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
