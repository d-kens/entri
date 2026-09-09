package com.entri.modules.users.validators;

import com.entri.users.validators.RoleValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoleValidatorTest {

    RoleValidator validator = new RoleValidator();

    @ParameterizedTest
    @ValueSource(strings = {"ORGANIZER", "ADMIN", "organizer", "admin", "Organizer", "Admin"})
    void isValid_knownRoleAnyCase_returnsTrue(String role) {
        assertThat(validator.isValid(role, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "SUPERADMIN", "", "  "})
    void isValid_unknownRole_returnsFalse(String role) {
        assertThat(validator.isValid(role, null)).isFalse();
    }

    @Test
    void isValid_nullRole_returnsFalse() {
        assertThat(validator.isValid(null, null)).isFalse();
    }
}
