package com.olexijko.paymentgw.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class PaymentProcessingResultDto {
    public static final String GENERIC_ERROR_KEY = "error";
    private final boolean isApproved;
    private final Map<String, String> errors;

    private PaymentProcessingResultDto(boolean isApproved, Map<String, String> errors) {
        this.isApproved = isApproved;
        this.errors = errors;
    }

    public static PaymentProcessingResultDto success() {
        return new PaymentProcessingResultDto(true, null);
    }

    public static PaymentProcessingResultDto failed(Map<String, String> errors) {
        return new PaymentProcessingResultDto(false, errors);
    }

    public static PaymentProcessingResultDto failed(String errorMessage) {
        return new PaymentProcessingResultDto(false, Map.of(GENERIC_ERROR_KEY, errorMessage));
    }
}
