package com.olexijko.paymentgw.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponseDto {
    private final String errorMessage;
}
