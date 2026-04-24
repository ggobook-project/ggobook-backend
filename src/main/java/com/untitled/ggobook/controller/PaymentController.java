package com.untitled.ggobook.controller;


import com.siot.IamportRestClient.exception.IamportResponseException;
import com.untitled.ggobook.domain.Payment;
import com.untitled.ggobook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

// 결제 컨트롤러
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<Payment> preparePayment(
            @AuthenticationPrincipal Long id,
            @RequestParam Integer amount
    ) {

        Payment payment = paymentService.preparePayment(id, amount);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @AuthenticationPrincipal Long id,
            @RequestBody Map<String, Object> request
    ) throws IamportResponseException, IOException {
        String impUid = (String) request.get("impUid");
        String merchantUid = (String) request.get("merchantUid");
        paymentService.verifyPayment(id, impUid, merchantUid);

        return ResponseEntity.ok("포인트 충전이 완료되었습니다.");
    }

}
