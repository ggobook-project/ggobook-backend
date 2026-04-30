package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Wallet;
import com.untitled.ggobook.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 지갑 컨트롤러
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<Integer> getWalletBalance(
            @AuthenticationPrincipal Long id
    ) {
        try {
            Wallet wallet = walletService.getWalletBalance(id);
            return ResponseEntity.ok(wallet.getBalance());
        } catch (RuntimeException e) {
            return ResponseEntity.ok(0);
        }
    }
}
