package com.olexijko.paymentgw.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
    @Pattern(regexp = "^[0-9]*[1-9]+$", message = "Amount is invalid.")
    private String amount;

    @NotBlank(message = "Currency is required.")
    private String currency;

    @Valid
    @NotNull(message = "Cardholder info is required.")
    private CardholderDto cardholder;

    @Valid
    @NotNull(message = "Card info is required.")
    private CardDto card;
}

