package com.entri.modules.validators;

import com.entri.validators.PhoneNumberValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhoneNumberValidatorTest {

    @Mock ConstraintValidatorContext context;
    @Mock ConstraintValidatorContext.ConstraintViolationBuilder builder;

    PhoneNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PhoneNumberValidator();
        when(context.buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0712345678", "0112345678", "254712345678", "254112345678", "+254712345678"})
    void isValid_validFormats_returnsTrue(String phoneNumber) {
        assertThat(validator.isValid(phoneNumber, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0812345678", "254812345678", "071234567", "07123456789", "abcdefghij", "12345", "+2547123456789"})
    void isValid_invalidFormats_returnsFalse(String phoneNumber) {
        assertThat(validator.isValid(phoneNumber, context)).isFalse();
    }

    @Test
    void isValid_nullValue_returnsFalse() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void isValid_blankValue_returnsFalse() {
        assertThat(validator.isValid("   ", context)).isFalse();
    }
}
