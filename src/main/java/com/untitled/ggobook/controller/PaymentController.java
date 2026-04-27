package com.untitled.ggobook.controller;


import com.siot.IamportRestClient.exception.IamportResponseException;
import com.untitled.ggobook.domain.Payment;
import com.untitled.ggobook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
            @RequestParam Integer amount,
            @RequestParam Integer pointAmount
    ) {

        Payment payment = paymentService.preparePayment(id, amount, pointAmount);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @AuthenticationPrincipal Long id,
            @RequestBody Map<String, Object> request
    ) throws IamportResponseException, IOException {
        String impUid = (String) request.get("impUid");
        String merchantUid = (String) request.get("merchantUid");

        try {
            paymentService.verifyPayment(id, impUid, merchantUid);
        } catch (ObjectOptimisticLockingFailureException e) {
            // 동시 요청 충돌 - 클라이언트에게 재시도 요청
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("잠시 후 다시 시도해주세요.");
        }

        return ResponseEntity.ok("포인트 충전이 완료되었습니다.");
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody Map<String, Object> request
    ) {
        String impUid = (String) request.get("imp_uid");
        String merchantUid = (String) request.get("merchant_uid");
        String status = (String) request.get("status");
        paymentService.handleWebhook(impUid, merchantUid, status);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<String> cancelPayment(
            @AuthenticationPrincipal Long id,
            @PathVariable Long paymentId
    ) throws IamportResponseException, IOException {
        paymentService.cancelPayment(id, paymentId);
        return ResponseEntity.ok("결제가 취소되었습니다.");
    }
}
