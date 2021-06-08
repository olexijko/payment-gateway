package com.olexijko.paymentgw.service;

import java.util.Optional;

import com.olexijko.paymentgw.dto.CardDto;
import com.olexijko.paymentgw.dto.CardholderDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResultDto;
import com.olexijko.paymentgw.entity.Card;
import com.olexijko.paymentgw.entity.Cardholder;
import com.olexijko.paymentgw.entity.Payment;
import com.olexijko.paymentgw.exception.DuplicatePaymentException;
import com.olexijko.paymentgw.exception.PaymentNotFoundException;
import com.olexijko.paymentgw.mapper.PaymentMapper;
import com.olexijko.paymentgw.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static com.olexijko.paymentgw.PayloadFactory.SANITISED_CARDHOLDER_NAME;
import static com.olexijko.paymentgw.PayloadFactory.SANITISED_CARD_PAN;
import static com.olexijko.paymentgw.PayloadFactory.SANITISED_CARD_EXPIRY_DATE;
import static com.olexijko.paymentgw.PayloadFactory.VALID_AMOUNT;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARDHOLDER_EMAIL;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARDHOLDER_NAME;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARD_PAN;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CURRENCY;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARD_CVV;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARD_EXPIRY_DATE;
import static com.olexijko.paymentgw.PayloadFactory.VALID_INVOICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PaymentServiceTest {
    private final PaymentRepository paymentRepositoryMock = mock(PaymentRepository.class);
    private final AuditSender auditSenderMock = mock(AuditSender.class);
    private final Encryptor encryptor = new Encryptor();

    private final PaymentService paymentService =
            new PaymentService(paymentRepositoryMock, new PaymentMapper(encryptor), auditSenderMock);

    @Test
    void processPayment_ReturnsSuccess_WhenPaymentInvoiceIsNew() {
        final PaymentDto inputPaymentDto = PaymentDto.builder()
                .invoice(VALID_INVOICE)
                .amount(VALID_AMOUNT)
                .currency(VALID_CURRENCY)
                .cardholder(CardholderDto.builder().email(VALID_CARDHOLDER_EMAIL).name(VALID_CARDHOLDER_NAME).build())
                .card(CardDto.builder().expiry(VALID_CARD_EXPIRY_DATE).pan(VALID_CARD_PAN).cvv(VALID_CARD_CVV).build())
                .build();
        when(paymentRepositoryMock.findByInvoice(inputPaymentDto.getInvoice())).thenReturn(Optional.empty());
        when(paymentRepositoryMock.save(ArgumentMatchers.any())).then(invocationOnMock -> invocationOnMock.getArgument(0));

        final PaymentProcessingResultDto paymentProcessingResult = paymentService.processPayment(inputPaymentDto);

        assertNotNull(paymentProcessingResult);
        assertTrue(paymentProcessingResult.isApproved());
        assertNull(paymentProcessingResult.getErrors());

        verify(paymentRepositoryMock).findByInvoice(inputPaymentDto.getInvoice());

        final ArgumentCaptor<Payment> savePaymentCapture = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepositoryMock).save(savePaymentCapture.capture());
        validatePaymentToSave(savePaymentCapture.getValue(), inputPaymentDto);

        final ArgumentCaptor<PaymentDto> sendPaymentToAuditCapture = ArgumentCaptor.forClass(PaymentDto.class);
        verify(auditSenderMock).sendPayment(sendPaymentToAuditCapture.capture());
        validateSentToAuditPaymentDto(sendPaymentToAuditCapture.getValue(), inputPaymentDto);

        verifyNoMoreInteractions(paymentRepositoryMock);
        verifyNoMoreInteractions(auditSenderMock);
    }

    @Test
    void processPayment_ThrowsDuplicatePaymentException_WhenPaymentInvoiceAlreadyProcessed() {
        final PaymentDto inputPaymentDto = PaymentDto.builder()
                .invoice(VALID_INVOICE)
                .amount(VALID_AMOUNT)
                .currency(VALID_CURRENCY)
                .cardholder(CardholderDto.builder().email(VALID_CARDHOLDER_EMAIL).name(VALID_CARDHOLDER_NAME).build())
                .card(CardDto.builder().expiry(VALID_CARD_EXPIRY_DATE).pan(VALID_CARD_PAN).cvv(VALID_CARD_CVV).build())
                .build();
        when(paymentRepositoryMock.findByInvoice(inputPaymentDto.getInvoice())).thenReturn(Optional.of(new Payment()));

        assertThrows(DuplicatePaymentException.class, () -> paymentService.processPayment(inputPaymentDto));

        verify(paymentRepositoryMock).findByInvoice(inputPaymentDto.getInvoice());
        verifyNoMoreInteractions(paymentRepositoryMock);
        verifyNoMoreInteractions(auditSenderMock);
    }

    @Test
    void findPaymentByInvoice_ReturnsFoundRecord_WhenInvoiceIsExisting() {
        final String invoice = VALID_INVOICE;
        final Payment paymentFromRepository = Payment.builder()
                .invoice(invoice)
                .amount(Integer.valueOf(VALID_AMOUNT))
                .currency(VALID_CURRENCY)
                .card(Card.builder().expiryDate(encryptor.encrypt(VALID_CARD_EXPIRY_DATE)).pan(encryptor.encrypt(VALID_CARD_PAN)).build())
                .cardholder(Cardholder.builder().name(encryptor.encrypt(VALID_CARDHOLDER_NAME)).email(VALID_CARDHOLDER_EMAIL).build())
                .build();
        when(paymentRepositoryMock.findByInvoice(invoice)).thenReturn(Optional.of(paymentFromRepository));

        final PaymentDto foundPaymentDto = paymentService.findPaymentByInvoice(invoice);

        validateFoundPayment(foundPaymentDto, paymentFromRepository);
        verify(paymentRepositoryMock).findByInvoice(invoice);
    }

    @Test
    void findPaymentByInvoice_ReturnsFoundRecord_WhenInvoiceIsNotExisting() {
        final String invoice = VALID_INVOICE;

        when(paymentRepositoryMock.findByInvoice(invoice)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.findPaymentByInvoice(invoice));
        verify(paymentRepositoryMock).findByInvoice(invoice);
    }

    private void validateFoundPayment(PaymentDto foundPaymentDto, Payment paymentFromRepository) {
        assertNotNull(foundPaymentDto);
        assertEquals(paymentFromRepository.getInvoice(), foundPaymentDto.getInvoice());
        assertEquals(String.valueOf(paymentFromRepository.getAmount()), foundPaymentDto.getAmount());
        assertEquals(paymentFromRepository.getCurrency(), foundPaymentDto.getCurrency());
        assertNotNull(foundPaymentDto.getCard());
        assertEquals(SANITISED_CARD_PAN, foundPaymentDto.getCard().getPan());
        assertEquals(SANITISED_CARD_EXPIRY_DATE, foundPaymentDto.getCard().getExpiry());
        assertNotNull(foundPaymentDto.getCardholder());
        assertEquals(SANITISED_CARDHOLDER_NAME, foundPaymentDto.getCardholder().getName());
        assertEquals(paymentFromRepository.getCardholder().getEmail(), foundPaymentDto.getCardholder().getEmail());
    }

    private void validatePaymentToSave(Payment paymentToSave, PaymentDto inputPaymentDto) {
        assertEquals(inputPaymentDto.getInvoice(), paymentToSave.getInvoice());
        assertEquals(Integer.valueOf(inputPaymentDto.getAmount()), paymentToSave.getAmount());
        assertEquals(inputPaymentDto.getCurrency(), paymentToSave.getCurrency());
        assertNotNull(paymentToSave.getCard());
        assertEquals(encryptor.encrypt(inputPaymentDto.getCard().getPan()), paymentToSave.getCard().getPan());
        assertEquals(encryptor.encrypt(inputPaymentDto.getCard().getExpiry()), paymentToSave.getCard().getExpiryDate());
        assertNotNull(paymentToSave.getCardholder());
        assertEquals(encryptor.encrypt(inputPaymentDto.getCardholder().getName()), paymentToSave.getCardholder().getName());
        assertEquals(inputPaymentDto.getCardholder().getEmail(), paymentToSave.getCardholder().getEmail());
    }

    private void validateSentToAuditPaymentDto(PaymentDto sentToAuditPaymentDto, PaymentDto inputPaymentDto) {
        assertEquals(inputPaymentDto.getInvoice(), sentToAuditPaymentDto.getInvoice());
        assertEquals(inputPaymentDto.getAmount(), sentToAuditPaymentDto.getAmount());
        assertEquals(inputPaymentDto.getCurrency(), sentToAuditPaymentDto.getCurrency());
        assertNotNull(sentToAuditPaymentDto.getCard());
        assertEquals(SANITISED_CARD_PAN, sentToAuditPaymentDto.getCard().getPan());
        assertEquals(SANITISED_CARD_EXPIRY_DATE, sentToAuditPaymentDto.getCard().getExpiry());
        assertNotNull(sentToAuditPaymentDto.getCardholder());
        assertEquals(SANITISED_CARDHOLDER_NAME, sentToAuditPaymentDto.getCardholder().getName());
        assertEquals(inputPaymentDto.getCardholder().getEmail(), sentToAuditPaymentDto.getCardholder().getEmail());
    }
}
