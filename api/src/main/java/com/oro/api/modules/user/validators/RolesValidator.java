package com.oro.api.modules.user.validators;

import com.oro.api.modules.user.repository.RoleRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class RolesValidator implements ConstraintValidator<ValidRoles, Set<String>> {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public boolean isValid(Set<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) return false;

        boolean allValid = values.stream()
                .allMatch(roleName -> roleRepository.existsByName(roleName));

        if (!allValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "One or more roles are invalid. Available roles: " + roleRepository.findAll()
            ).addConstraintViolation();
        }

        return allValid;
    }
}