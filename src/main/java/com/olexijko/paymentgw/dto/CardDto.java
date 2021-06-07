package com.olexijko.paymentgw.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.olexijko.paymentgw.validator.ValidExpiryDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.LuhnCheck;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class CardDto {
    @NotBlank(message = "PAN is required.")
    @Pattern(regexp = "[0-9]{16}", message = "PAN is invalid.")
    @LuhnCheck(message = "PAN is invalid.")
    private String pan;

    @ValidExpiryDate
    private String expiry;

    @NotBlank(message = "CVV is required.")
    private String cvv;
}
