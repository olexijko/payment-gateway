package com.olexijko.paymentgw.mapper;

import com.olexijko.paymentgw.dto.CardDto;
import com.olexijko.paymentgw.dto.CardholderDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.entity.Card;
import com.olexijko.paymentgw.entity.Cardholder;
import com.olexijko.paymentgw.entity.Payment;
import com.olexijko.paymentgw.service.Encryptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    private static final String SANITIZE_SYMBOL = "*";
    private static final int PAN_LAST_VISIBLE_SYMBOLS_COUNT = 4;

    private final Encryptor encryptor;

    public PaymentMapper(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    public Payment toEntityFromDto(PaymentDto dto) {
        return dto == null ? null : Payment.builder()
                .invoice(dto.getInvoice())
                .amount(dto.getAmount() == null ? null : Integer.valueOf(dto.getAmount()))
                .currency(dto.getCurrency())
                .card(toCardEntityFromDto(dto.getCard()))
                .cardholder(toCardholderEntityFromDto(dto.getCardholder()))
                .build();
    }

    public PaymentDto toDtoFromEntity(Payment entity) {
        return entity == null ? null : PaymentDto.builder()
                .invoice(entity.getInvoice())
                .amount(entity.getAmount() == null ? null : String.valueOf(entity.getAmount()))
                .currency(entity.getCurrency())
                .card(toCardDtoFromEntity(entity.getCard()))
                .cardholder(toCardholderDtoFromEntity(entity.getCardholder()))
                .build();
    }

    private CardholderDto toCardholderDtoFromEntity(Cardholder entity) {
        return entity == null ? null : CardholderDto.builder()
                .email(entity.getEmail())
                .name(sanitizeEntireValue(encryptor.decrypt(entity.getName())))
                .build();
    }

    private CardDto toCardDtoFromEntity(Card entity) {
        return entity == null ? null : CardDto.builder()
                .pan(sanitizePan(encryptor.decrypt(entity.getPan())))
                .expiry(sanitizeEntireValue(encryptor.decrypt(entity.getExpiryDate())))
                .build();
    }

    private String sanitizeEntireValue(String value) {
        return StringUtils.isNotEmpty(value) ? SANITIZE_SYMBOL.repeat(value.length()) : StringUtils.EMPTY;
    }

    private String sanitizePan(String pan) {
        if (StringUtils.isEmpty(pan)) {
            return StringUtils.EMPTY;
        }
        final int symbolsCountToSanitize = pan.length() - PAN_LAST_VISIBLE_SYMBOLS_COUNT;
        return SANITIZE_SYMBOL.repeat(symbolsCountToSanitize) + pan.substring(symbolsCountToSanitize);
    }

    private Cardholder toCardholderEntityFromDto(CardholderDto dto) {
        return dto == null ? null : Cardholder.builder()
                .name(encryptor.encrypt(dto.getName()))
                .email(dto.getEmail())
                .build();
    }

    private Card toCardEntityFromDto(CardDto dto) {
        return dto == null ? null : Card.builder()
                .pan(encryptor.encrypt(dto.getPan()))
                .expiryDate(encryptor.encrypt(dto.getExpiry()))
                .build();
    }
}
