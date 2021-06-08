package com.olexijko.paymentgw.service;

import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResultDto;
import com.olexijko.paymentgw.entity.Payment;
import com.olexijko.paymentgw.exception.DuplicatePaymentException;
import com.olexijko.paymentgw.exception.PaymentNotFoundException;
import com.olexijko.paymentgw.mapper.PaymentMapper;
import com.olexijko.paymentgw.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AuditSender auditSender;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, AuditSender auditSender) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.auditSender = auditSender;
    }

    public PaymentProcessingResultDto processPayment(PaymentDto paymentDto) {
        //TODO: validation for duplicates may require another approach if search by invoice will not satisfy timing for Payment
        // processing
        if (paymentRepository.findByInvoice(paymentDto.getInvoice()).isPresent()) {
            throw new DuplicatePaymentException(
                    String.format("Payment with invoice '%s' was previously processed", paymentDto.getInvoice()));
        }
        final Payment savedPayment = paymentRepository.save(paymentMapper.toEntityFromDto(paymentDto));
        auditSender.sendPayment(paymentMapper.toDtoFromEntity(savedPayment));
        return PaymentProcessingResultDto.success();
    }

    public PaymentDto findPaymentByInvoice(String invoice) {
        final Payment payment = paymentRepository.findByInvoice(invoice)
                .orElseThrow(() -> new PaymentNotFoundException(String.format("There is no processed payment with invoice '%s'", invoice)));
        return paymentMapper.toDtoFromEntity(payment);
    }
}
