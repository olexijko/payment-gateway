package com.olexijko.paymentgw.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class PaymentProcessingResponseDto {
    private final boolean isApproved;
    private final Map<String, String> errors;

    private PaymentProcessingResponseDto(boolean isApproved, Map<String, String> errors) {
        this.isApproved = isApproved;
        this.errors = errors;
    }

    public static PaymentProcessingResponseDto success() {
        return new PaymentProcessingResponseDto(true, null);
    }

    public static PaymentProcessingResponseDto failed(Map<String, String> errors) {
        return new PaymentProcessingResponseDto(false, errors);
    }
}
