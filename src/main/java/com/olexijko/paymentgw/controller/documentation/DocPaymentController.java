package com.olexijko.paymentgw.controller.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payments", description = "expose the API to send credit card payments for processing and retrieve transaction history")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocPaymentController {
}
