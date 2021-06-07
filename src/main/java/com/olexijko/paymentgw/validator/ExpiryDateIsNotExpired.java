package com.olexijko.paymentgw.validator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExpiryDateIsNotExpired implements ConstraintValidator<ValidExpiryDate, String> {

    public static final String EXPIRY_DATE_FORMAT = "MMyy";

    @Override
    public void initialize(ValidExpiryDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(String expiryDateStr, ConstraintValidatorContext context) {
        try {
            final LocalDate expiryDate = YearMonth.parse(expiryDateStr, DateTimeFormatter.ofPattern(EXPIRY_DATE_FORMAT)).atEndOfMonth();
            return !expiryDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            //skip this validation in case input value is not understandable.
            return true;
        }
    }
}
