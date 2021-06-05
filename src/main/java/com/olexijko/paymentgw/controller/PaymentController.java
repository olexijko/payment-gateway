package com.olexijko.paymentgw.controller;

import com.olexijko.paymentgw.dto.PaymentProcessingResponseDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.olexijko.paymentgw.controller.PaymentController.BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(BASE_PATH)
public class PaymentController {
    static final String BASE_PATH = "/api/v1/payments";

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public PaymentProcessingResponseDto processNewPayment(@RequestBody PaymentDto paymentDto) {
        return paymentService.processPayment(paymentDto);
    }

    @GetMapping(value = "/{invoiceNumber}", produces = APPLICATION_JSON_VALUE)
    public PaymentDto getPaymentByInvoiceNumber(@PathVariable String invoiceNumber) {
        return paymentService.findPaymentByInvoice(invoiceNumber);
    }

}
