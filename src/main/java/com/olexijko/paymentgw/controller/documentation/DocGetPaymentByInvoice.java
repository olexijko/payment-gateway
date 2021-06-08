package com.olexijko.paymentgw.controller.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.olexijko.paymentgw.dto.ApiErrorResponseDto;
import com.olexijko.paymentgw.dto.PaymentDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Operation(summary = "Retrieve processed payment transaction by invoice number")
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Payment transaction is found.",
                content = {
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PaymentDto.class))
                }),
        @ApiResponse(
                responseCode = "404",
                description = "There is no payment transaction with given invoice number.",
                content = {
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiErrorResponseDto.class))
                })
})
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocGetPaymentByInvoice {
}
