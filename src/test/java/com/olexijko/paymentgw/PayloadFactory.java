package com.olexijko.paymentgw;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PayloadFactory {
    public static final String VALID_INVOICE = "12345";
    public static final String VALID_AMOUNT = "123";
    public static final String VALID_CURRENCY = "USD";

    public static final String VALID_CARD_PAN = "4532011283777270";
    public static final String SANITISED_CARD_PAN = "************7270";
    public static final String CARD_PAN_INVALID_LUHN_CHECK = "4200000000000001";
    public static final String VALID_CARD_EXPIRY_DATE = LocalDate.now().plus(1, ChronoUnit.YEARS).format(DateTimeFormatter.ofPattern("MMyy"));
    public static final String EXPIRED_CARD_EXPIRY_DATE = LocalDate.now().minus(1, ChronoUnit.YEARS).format(DateTimeFormatter.ofPattern("MMyy"));
    public static final String SANITISED_CARD_EXPIRY_DATE = "****";
    public static final String VALID_CARD_CVV = "123";

    public static final String VALID_CARDHOLDER_NAME = "First Last";
    public static final String VALID_CARDHOLDER_EMAIL = "email@domain.com";
    public static final String SANITISED_CARDHOLDER_NAME = "**********";

}
