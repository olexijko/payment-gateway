package com.olexijko.paymentgw.repository;

import java.util.Optional;

import com.olexijko.paymentgw.entity.Payment;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Optional<Payment> findByInvoice(String invoice);
}
