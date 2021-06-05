package com.olexijko.paymentgw.service;

import com.olexijko.paymentgw.dto.PaymentProcessingResponseDto;
import com.olexijko.paymentgw.dto.PaymentDto;
import com.olexijko.paymentgw.mapper.PaymentMapper;
import com.olexijko.paymentgw.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    public PaymentProcessingResponseDto processPayment(PaymentDto paymentDto) {
        paymentRepository.save(paymentMapper.toEntityFromDto(paymentDto));
        return PaymentProcessingResponseDto.success();
    }

    public PaymentDto findPaymentByInvoice(String invoice) {
        return paymentMapper.toDtoFromEntity(paymentRepository.findByInvoice(invoice));
    }
}
