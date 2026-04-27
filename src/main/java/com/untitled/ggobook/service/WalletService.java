package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.Wallet;
import com.untitled.ggobook.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 지갑 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public Wallet getWalletBalance(Long id) {

        return walletRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("User가 존재하지 않습니다."));
    }
}
