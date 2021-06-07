package com.olexijko.paymentgw.service;

import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.dto.PaymentProcessingResponseDto;
import com.olexijko.paymentgw.entity.Payment;
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

    public PaymentProcessingResponseDto processPayment(PaymentDto paymentDto) {
        final Payment savedPayment = paymentRepository.save(paymentMapper.toEntityFromDto(paymentDto));
        auditSender.sendPayment(paymentMapper.toDtoFromEntity(savedPayment));
        return PaymentProcessingResponseDto.success();
    }

    public PaymentDto findPaymentByInvoice(String invoice) {
        return paymentMapper.toDtoFromEntity(paymentRepository.findByInvoice(invoice));
    }
}
