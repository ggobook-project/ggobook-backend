package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Point;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.Wallet;
import com.untitled.ggobook.repository.PointRepository;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 포인트 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<Point> getPointHistory(Long id) {
        Wallet wallet = walletRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Wallet이 존재하지 않습니다."));
        return pointRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }

//    public ResponseEntity<Point> getPonitBalance(Long id) {
//        User user = userRepository.findById(id);
//        return pointRepository.findByUser(user);
//    }
}
