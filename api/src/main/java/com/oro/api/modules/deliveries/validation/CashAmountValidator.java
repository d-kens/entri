package com.oro.api.modules.deliveries.validation;

import com.oro.api.modules.deliveries.dto.CreateDeliveryDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class CashAmountValidator
        implements ConstraintValidator<ValidCashAmount, CreateDeliveryDto> {

    @Override
    public boolean isValid(CreateDeliveryDto dto, ConstraintValidatorContext context) {

        if (dto == null) {
            return true;
        }

        if (Boolean.TRUE.equals(dto.collectCash())) {

            if (dto.cashAmount() == null
                    || dto.cashAmount().compareTo(BigDecimal.ZERO) <= 0) {

                context.disableDefaultConstraintViolation();

                context.buildConstraintViolationWithTemplate(
                                "cashAmount must be greater than 0 when collectCash is true"
                        )
                        .addPropertyNode("cashAmount")
                        .addConstraintViolation();

                return false;
            }
        }

        return true;
    }
}

