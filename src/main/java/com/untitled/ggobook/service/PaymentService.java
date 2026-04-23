package com.untitled.ggobook.service;

import com.untitled.ggobook.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 결제 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;


    public void chargePoint(Long userId, Integer amount) {
    }
}
