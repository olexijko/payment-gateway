package com.olexijko.paymentgw.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.olexijko.paymentgw.controller.documentation.DocGetPaymentByInvoice;
import com.olexijko.paymentgw.controller.documentation.DocPaymentController;
import com.olexijko.paymentgw.controller.documentation.DocProcessNewPayment;
import com.olexijko.paymentgw.dto.ApiErrorResponseDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResultDto;
import com.olexijko.paymentgw.exception.DuplicatePaymentException;
import com.olexijko.paymentgw.exception.PaymentNotFoundException;
import com.olexijko.paymentgw.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.olexijko.paymentgw.controller.PaymentController.BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(BASE_PATH)
@DocPaymentController
public class PaymentController {
    static final String BASE_PATH = "/api/v1/payments";

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @DocProcessNewPayment
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public PaymentProcessingResultDto processNewPayment(@RequestBody @Valid PaymentDto paymentDto) {
        return paymentService.processPayment(paymentDto);
    }

    @DocGetPaymentByInvoice
    @GetMapping(value = "/{invoiceNumber}", produces = APPLICATION_JSON_VALUE)
    public PaymentDto getPaymentByInvoiceNumber(@PathVariable @NotBlank String invoiceNumber) {
        return paymentService.findPaymentByInvoice(invoiceNumber);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public PaymentProcessingResultDto handleValidationExceptions(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return PaymentProcessingResultDto.failed(errors);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PaymentNotFoundException.class)
    public ApiErrorResponseDto handlePaymentNotFoundException(PaymentNotFoundException e) {
        return new ApiErrorResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicatePaymentException.class)
    public PaymentProcessingResultDto handleDuplicatePaymentException(DuplicatePaymentException e) {
        return PaymentProcessingResultDto.failed(e.getMessage());
    }
}
