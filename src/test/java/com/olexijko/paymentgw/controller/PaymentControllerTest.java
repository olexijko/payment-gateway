package com.olexijko.paymentgw.controller;

import java.util.Map;

import com.olexijko.paymentgw.dto.CardDto;
import com.olexijko.paymentgw.dto.CardholderDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResponseDto;
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
        Mockito.when(paymentServiceMock.processPayment(ArgumentMatchers.any())).thenReturn(PaymentProcessingResponseDto.success());

        final Map<String, Object> requestBody = Map.of(
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", "1299a",
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", -1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", 0,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0624",
                        "cvv", "789")

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
    void processNewPayment_ReturnsBadRequest_WhenMandatoryCardFieldsAreEmpty() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "invalid.email"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "a53201a28377727a",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4200000000000001",
                        "expiry", "0624",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "0619",
                        "cvv", "789")

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
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
                "card", Map.of(
                        "pan", "4532011283777270",
                        "expiry", "1321",
                        "cvv", "789")

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
    void processNewPayment_ReturnsBadRequest_WhenMandatoryCardholderFieldsAreEmpty() throws Exception {
        final Map<String, Object> requestBody = Map.of(
                "invoice", 1234567,
                "amount", 1299,
                "currency", "EUR",
                "cardholder", Map.of(
                        "name", "First Last",
                        "email", "email@domain.com"),
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
                .invoice("12345")
                .amount("1299")
                .currency("USD")
                .cardholder(CardholderDto.builder().email("email@domain.com").name("**********").build())
                .card(CardDto.builder().expiry("****").pan("************0001").build())
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
