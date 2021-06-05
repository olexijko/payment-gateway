package com.olexijko.paymentgw.repository;

import com.olexijko.paymentgw.entity.Payment;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Payment findByInvoice(String invoice);
}
