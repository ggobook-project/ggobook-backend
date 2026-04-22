package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    // 유저 1명당 지갑 1개만 가져야 하므로 unique = true 설정이 핵심입니다!
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Integer balance = 0; // 초기 잔액은 0원

    // 🌟 대기업식 동시성 방어 (Optimistic Lock)
    // 두 명이 동시에 결제하려고 할 때, 이 version이 다르면 에러를 발생시켜 돈 복사를 막습니다.
    @Version
    private Long version;
}