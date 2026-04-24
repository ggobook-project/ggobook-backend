package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByMerchantUid(String merchantUid);
}
