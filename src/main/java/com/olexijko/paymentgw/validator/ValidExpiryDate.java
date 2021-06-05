package com.olexijko.paymentgw.validator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotBlank(message = "Expiry is required.")
@Pattern(regexp = "^(0[1-9]|1[012])[0-9]{2}$", message = "Expiry should be in format 'MMyy'")
@Constraint(validatedBy = ExpiryDateIsNotExpired.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ValidExpiryDate {
    String message() default "Payment card is expired.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
