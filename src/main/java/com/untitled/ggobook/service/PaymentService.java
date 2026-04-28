package com.untitled.ggobook.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
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
    public Payment preparePayment(Long id, Integer amount, Integer pointAmount) {

        String merchantUid = generateMerchantUid();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User가 존재하지 않습니다."));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet이 존재하지 않습니다."));

        Payment payment = new Payment();

        payment.setUser(user);
        payment.setStatus("PENDING");
        payment.setAmount(amount);
        payment.setPointAmount(pointAmount);
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
        wallet.setBalance(wallet.getBalance() + paymentOur.getPointAmount());
        walletRepository.save(wallet);

        paymentOur.setImpUid(impUid);
        paymentOur.setStatus("COMPLETE");
        paymentRepository.save(paymentOur);

        Point point = new Point();
        point.setAmount(paymentOur.getPointAmount());;
        point.setPointType("CHARGE");
        point.setUser(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User가 존재하지 않습니다.")));
        point.setWallet(wallet);
        point.setDescription("포인트 충전");
        point.setPayment(paymentOur);
        pointRepository.save(point);
    }

    @Transactional
    public void handleWebhook(String impUid, String merchantUid, String status) {

        // 1. merchantUid로 우리 Payment 조회
        Payment paymentOur = paymentRepository.findByMerchantUid(merchantUid);
        if (paymentOur == null) {
            log.warn("웹훅 - 존재하지 않는 merchantUid: {}", merchantUid);
            return;
        }

        // 2. 이미 처리된 결제면 중복 처리 방지
        if ("COMPLETE".equals(paymentOur.getStatus())) {
            log.info("웹훅 - 이미 처리된 결제: {}", merchantUid);
            return;
        }

        // 3. 포트원에 실제 결제 검증
        try {
            IamportClient iamportClient = new IamportClient(apiKey, apiSecret);
            IamportResponse<com.siot.IamportRestClient.response.Payment> response =
                    iamportClient.paymentByImpUid(impUid);
            com.siot.IamportRestClient.response.Payment portOnePayment = response.getResponse();

            // 4. 결제 상태 확인
            if (!"paid".equals(portOnePayment.getStatus())) {
                paymentOur.setStatus("FAILED");
                paymentRepository.save(paymentOur);
                log.warn("웹훅 - 결제 미완료 상태: {}", portOnePayment.getStatus());
                return;
            }

            // 5. 금액 검증
            if (!portOnePayment.getAmount().equals(BigDecimal.valueOf(paymentOur.getAmount()))) {
                paymentOur.setStatus("FAILED");
                paymentRepository.save(paymentOur);
                log.warn("웹훅 - 금액 불일치: {}", merchantUid);
                return;
            }

            // 6. Wallet 잔액 증가
            Wallet wallet = paymentOur.getWallet();
            wallet.setBalance(wallet.getBalance() + paymentOur.getPointAmount());
            walletRepository.save(wallet);

            // 7. Payment 상태 업데이트
            paymentOur.setImpUid(impUid);
            paymentOur.setStatus("COMPLETE");
            paymentRepository.save(paymentOur);

            // 8. Point 내역 저장
            Point point = new Point();
            point.setWallet(wallet);
            point.setUser(paymentOur.getUser());
            point.setPointType("CHARGE");
            point.setAmount(paymentOur.getPointAmount());
            point.setDescription("포인트 충전 (웹훅)");
            point.setPayment(paymentOur);
            pointRepository.save(point);

            log.info("웹훅 - 결제 처리 완료: {}", merchantUid);

        } catch (IamportResponseException | IOException e) {
            log.error("웹훅 - 포트원 검증 실패: {}", e.getMessage());
            throw new RuntimeException("웹훅 처리 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelPayment(Long id, Long paymentId) throws IamportResponseException, IOException {

        Payment paymentOur = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 내역이 존재하지 않습니다."));

        if (!paymentOur.getUser().getId().equals(id)) {
            throw new RuntimeException("본인의 결제만 취소할 수 있습니다.");
        }

        if ("CANCEL".equals(paymentOur.getStatus())) {
            throw new RuntimeException("이미 취소된 결제입니다.");
        }

        if (!"COMPLETE".equals(paymentOur.getStatus())) {
            throw new RuntimeException("완료된 결제만 취소할 수 있습니다.");
        }

        IamportClient iamportClient = new IamportClient(apiKey, apiSecret);
        CancelData cancelData = new CancelData(paymentOur.getImpUid(), true);
        IamportResponse<com.siot.IamportRestClient.response.Payment> response =
                iamportClient.cancelPaymentByImpUid(cancelData);

        if (response.getCode() != 0) {
            throw new RuntimeException("포트원 취소 실패: " + response.getMessage());
        }

        paymentOur.setStatus("CANCEL");
        paymentOur.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(paymentOur);

        Wallet wallet = paymentOur.getWallet();
        if (wallet.getBalance() < paymentOur.getPointAmount()) {
            throw new RuntimeException("사용한 포인트가 있어 취소할 수 없습니다.");
        }
        wallet.setBalance(wallet.getBalance() - paymentOur.getPointAmount());
        walletRepository.save(wallet);

        Point point = new Point();
        point.setUser(paymentOur.getUser());
        point.setWallet(wallet);
        point.setPointType("DEDUCT");
        point.setAmount(paymentOur.getPointAmount());
        point.setDescription("결제 취소 - " + paymentOur.getMerchantUid());
        pointRepository.save(point);
    }
}
