package com.untitled.ggobook.controller;


import com.untitled.ggobook.domain.Payment;
import com.untitled.ggobook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 결제 컨트롤러
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/charge")
    public Payment chargePoint(
            @AuthenticationPrincipal Long userId,
            @RequestParam Integer amount
    ){
        paymentService.chargePoint(userId, amount);

        return
    }
}
