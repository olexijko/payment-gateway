package com.olexijko.paymentgw.controller.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResultDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Operation(summary = "Submit a payment for processing")
@RequestBody(content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentDto.class)))
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Payment processed successfully.",
                content = {
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PaymentProcessingResultDto.class))
                }),
        @ApiResponse(
                responseCode = "409",
                description = "Payment processing rejected because another payment with given 'invoice number' has been already processed.",
                content = {
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PaymentProcessingResultDto.class))
                }),
        @ApiResponse(
                responseCode = "400",
                description = "Cannot process the given request because it is invalid.",
                content = {
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PaymentProcessingResultDto.class))
                })
})
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocProcessNewPayment {
}
