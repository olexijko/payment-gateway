package com.olexijko.paymentgw.controller;

import java.util.Map;

import com.olexijko.paymentgw.dto.CardDto;
import com.olexijko.paymentgw.dto.CardholderDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResultDto;
import com.olexijko.paymentgw.exception.PaymentNotFoundException;
import com.olexijko.paymentgw.service.PaymentService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.olexijko.paymentgw.PayloadFactory.CARD_PAN_INVALID_LUHN_CHECK;
import static com.olexijko.paymentgw.PayloadFactory.EXPIRED_CARD_EXPIRY_DATE;
import static com.olexijko.paymentgw.PayloadFactory.SANITISED_CARDHOLDER_NAME;
import static com.olexijko.paymentgw.PayloadFactory.SANITISED_CARD_EXPIRY_DATE;
import static com.olexijko.paymentgw.PayloadFactory.SANITISED_CARD_PAN;
import static com.olexijko.paymentgw.PayloadFactory.VALID_AMOUNT;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARDHOLDER_EMAIL;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARDHOLDER_NAME;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARD_CVV;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARD_EXPIRY_DATE;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CARD_PAN;
import static com.olexijko.paymentgw.PayloadFactory.VALID_CURRENCY;
import static com.olexijko.paymentgw.PayloadFactory.VALID_INVOICE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {
    private static final String PAYMENT_CONTROLLER_BASE_PATH = "/api/v1/payments";

    private final PaymentService paymentServiceMock = Mockito.mock(PaymentService.class);
    private final PaymentController paymentController = new PaymentController(paymentServiceMock);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsApproved_WhenValidDataIsSent() throws Exception {
        Mockito.when(paymentServiceMock.processPayment(ArgumentMatchers.any())).thenReturn(PaymentProcessingResultDto.success());

        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.approved").value(true));

        Mockito.verify(paymentServiceMock).processPayment(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenMandatoryPaymentFieldsAreEmpty() throws Exception {
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject().toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.invoice").value("Invoice is required."))
                .andExpect(jsonPath("$.errors.amount").value("Amount is required."))
                .andExpect(jsonPath("$.errors.currency").value("Currency is required."))
                .andExpect(jsonPath("$.errors.card").value("Card info is required."))
                .andExpect(jsonPath("$.errors.cardholder").value("Cardholder info is required."));

        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenPaymentAmountHasNonNumericSymbols() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", "1299a",
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);


        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.amount").value("Amount is invalid."));

        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenPaymentAmountIsNegative() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", -1299,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);


        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.amount").value("Amount is invalid."));

        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenPaymentAmountIsZero() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", 0,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);


        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.amount").value("Amount is invalid."));

        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenMandatoryCardholderFieldsAreEmpty() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['cardholder.email']").value("Email is required."))
                .andExpect(jsonPath("$.errors.['cardholder.name']").value("Name is required."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenCardholderEmailIsInvalid() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", "invalid.email"),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['cardholder.email']").value("Invalid cardholder email format."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenCardPanHasNonNumericSymbols() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", "4532011b8377727a",
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['card.pan']").value("PAN is invalid."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenCardPanLuhnCheckIsFailed() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", CARD_PAN_INVALID_LUHN_CHECK,
                        "expiry", VALID_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['card.pan']").value("PAN is invalid."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenCardIsExpired() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", EXPIRED_CARD_EXPIRY_DATE,
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['card.expiry']").value("Payment card is expired."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenCardExpiryIsInvalidDate() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of(
                        "pan", VALID_CARD_PAN,
                        "expiry", "1321",
                        "cvv", VALID_CARD_CVV)

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['card.expiry']").value("Expiry should be in format 'MMyy'."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void processNewPayment_ReturnsBadRequest_WhenMandatoryCardFieldsAreEmpty() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", VALID_INVOICE,
                "amount", VALID_AMOUNT,
                "currency", VALID_CURRENCY,
                "cardholder", Map.of(
                        "name", VALID_CARDHOLDER_NAME,
                        "email", VALID_CARDHOLDER_EMAIL),
                "card", Map.of()

        );
        final RequestBuilder request = post(PAYMENT_CONTROLLER_BASE_PATH).
                content(new JSONObject(requestBody).toString()).contentType(APPLICATION_JSON);

        this.mockMvc.perform(request).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.errors.['card.pan']").value("PAN is required."))
                .andExpect(jsonPath("$.errors.['card.cvv']").value("CVV is required."))
                .andExpect(jsonPath("$.errors.['card.expiry']").value("Expiry is required."));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void getPaymentByInvoiceNumber_ReturnsFoundPayment_WhenValidInvoiceIsSent() throws Exception {
        final PaymentDto foundPayment = PaymentDto.builder()
                .invoice(VALID_INVOICE)
                .amount(VALID_AMOUNT)
                .currency(VALID_CURRENCY)
                .cardholder(CardholderDto.builder().email(VALID_CARDHOLDER_EMAIL).name(SANITISED_CARDHOLDER_NAME).build())
                .card(CardDto.builder().expiry(SANITISED_CARD_EXPIRY_DATE).pan(SANITISED_CARD_PAN).build())
                .build();
        final String invoice = foundPayment.getInvoice();
        Mockito.when(paymentServiceMock.findPaymentByInvoice(invoice)).thenReturn(foundPayment);

        this.mockMvc.perform(get(PAYMENT_CONTROLLER_BASE_PATH + "/" + invoice))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.invoice").value(foundPayment.getInvoice()))
                .andExpect(jsonPath("$.amount").value(foundPayment.getAmount()))
                .andExpect(jsonPath("$.currency").value(foundPayment.getCurrency()))
                .andExpect(jsonPath("$.cardholder.email").value(foundPayment.getCardholder().getEmail()))
                .andExpect(jsonPath("$.cardholder.name").value(foundPayment.getCardholder().getName()))
                .andExpect(jsonPath("$.card.pan").value(foundPayment.getCard().getPan()))
                .andExpect(jsonPath("$.card.expiry").value(foundPayment.getCard().getExpiry()))
                .andExpect(jsonPath("$.card.cvv").doesNotExist());
        Mockito.verify(paymentServiceMock).findPaymentByInvoice(invoice);
    }

    @Test
    void getPaymentByInvoiceNumber_ReturnsNotFoundError_WhenPaymentIsNotFound() throws Exception {
        final String notExistingInvoice = "1234";
        final String message = "Payment Not Found";
        Mockito.when(paymentServiceMock.findPaymentByInvoice(notExistingInvoice)).thenThrow(new PaymentNotFoundException(message));

        this.mockMvc.perform(get(PAYMENT_CONTROLLER_BASE_PATH + "/" + notExistingInvoice))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound()).andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.error").value(message));
        Mockito.verify(paymentServiceMock).findPaymentByInvoice(notExistingInvoice);
    }

}
