package com.olexijko.paymentgw.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class PaymentDto {
    @NotBlank(message = "Invoice is required.")
    private String invoice;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount should have a positive value.")
    private Double amount;

    @NotBlank(message = "Currency is required.")
    private String currency;

    @Valid
    @NotNull(message = "Cardholder info is required.")
    private CardholderDto cardholder;

    @Valid
    @NotNull(message = "Card info is required.")
    private CardDto card;
}

