package com.untitled.ggobook.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.untitled.ggobook.domain.Payment;
import com.untitled.ggobook.domain.Point;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.Wallet;
import com.untitled.ggobook.repository.PaymentRepository;
import com.untitled.ggobook.repository.PointRepository;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// 결제 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PointRepository pointRepository;

    @Value("${PORTONE_API_SECRET}")
    private String apiSecret;

    @Value("${PORTONE_API_KEY}")
    private String apiKey;

    @Transactional
    public Payment preparePayment(Long id, Integer amount) {

        String merchantUid = generateMerchantUid();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User가 존재하지 않습니다."));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet이 존재하지 않습니다."));

        Payment payment = new Payment();

        payment.setUser(user);
        payment.setStatus("PENDING");
        payment.setAmount(amount);
        payment.setPointAmount(amount);
        payment.setMerchantUid(merchantUid);
        payment.setWallet(wallet);
        payment.setPaymentMethod("CARD");


        return paymentRepository.save(payment);
    }

    private String generateMerchantUid() {
        String uniqueString = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String formattedDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return formattedDay + '-' + uniqueString;
    }

    @Value("${portone.test-mode:false}")
    private boolean testMode;

    @Transactional
    public void verifyPayment(Long id, String impUid, String merchantUid) throws IamportResponseException, IOException {

        // Payment 행 락 획득
        Payment paymentOur = paymentRepository.findByMerchantUidWithLock(merchantUid);
        if (paymentOur == null) {
            throw new RuntimeException("결제 정보를 찾을 수 없습니다: " + merchantUid);
        }

        // 중복 처리 방지 (이미 완료된 결제 재요청 차단)
        if ("COMPLETE".equals(paymentOur.getStatus())) {
            return;
        }

        if (!testMode) {
            IamportClient iamportClient = new IamportClient(apiKey, apiSecret);
            IamportResponse<com.siot.IamportRestClient.response.Payment> response = iamportClient.paymentByImpUid(impUid);
            com.siot.IamportRestClient.response.Payment payment = response.getResponse();

            if (!payment.getAmount().equals(BigDecimal.valueOf(paymentOur.getAmount()))) {
                paymentOur.setStatus("FAILED");
                paymentRepository.save(paymentOur);
                throw new RuntimeException("결제 금액 불일치!");
            }
        }

        // Wallet 행 락 획득 후 잔액 업데이트
        Wallet wallet = paymentOur.getWallet();
        wallet.setBalance(wallet.getBalance() + paymentOur.getAmount());
        walletRepository.save(wallet);

        paymentOur.setImpUid(impUid);
        paymentOur.setStatus("COMPLETE");
        paymentRepository.save(paymentOur);

        Point point = new Point();
        point.setAmount(paymentOur.getAmount());
        point.setPointType("CHARGE");
        point.setUser(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User가 존재하지 않습니다.")));
        point.setWallet(wallet);
        point.setDescription("포인트 충전");
        pointRepository.save(point);
    }
}
